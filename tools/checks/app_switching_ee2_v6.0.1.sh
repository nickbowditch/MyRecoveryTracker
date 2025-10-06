#!/bin/sh
set -eu

PKG="com.nick.myrecoverytracker"
RCV="$PKG/.TriggerReceiver"
ACT_SWITCH="$PKG.ACTION_RUN_APP_SWITCHING_DAILY"
OUT="evidence/v6.0/app_switching/ee2.txt"
CSV="files/daily_app_switching.csv"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "EE-2 RESULT=FAIL (no device/emulator)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "EE-2 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

adb shell run-as "$PKG" rm -f "$CSV" >/dev/null 2>&1 || true
adb logcat -c >/dev/null 2>&1 || true

adb shell cmd activity broadcast -n "$RCV" -a "$ACT_SWITCH" --receiver-foreground --user 0 >/dev/null 2>&1 || true
sleep 2

HDR="$(adb exec-out run-as "$PKG" sed -n '1p' "$CSV" 2>/dev/null | tr -d '\r' || true)"
ROWS="$(adb exec-out run-as "$PKG" head -n5 "$CSV" 2>/dev/null | tr -d '\r' || true)"
LOGS="$(adb logcat -d 2>/dev/null | grep -iE 'AppSwitching|TriggerReceiver|WorkManager|enqueue|worker' | tail -n 40 || true)"

{
echo "=== HEADER ==="
echo "${HDR:-[missing]}"
echo
echo "=== FIRST 5 ROWS ==="
[ -n "$ROWS" ] && echo "$ROWS" || echo "[none]"
echo
echo "=== LOGCAT ==="
[ -n "$LOGS" ] && echo "$LOGS" || echo "[none]"
} | tee "$OUT" >/dev/null

if [ "$HDR" = "date,switches,entropy" ]; then
echo "EE-2 RESULT=PASS" | tee -a "$OUT"; exit 0
else
echo "EE-2 RESULT=FAIL" | tee -a "$OUT"; exit 1
fi
