#!/bin/sh
set -eu

PKG="com.nick.myrecoverytracker"
RCV="$PKG/.TriggerReceiver"
ACT="$PKG.ACTION_RUN_APP_SWITCHING_DAILY"
CSV_OUT="files/daily_app_switching.csv"
CSV_IN="files/usage_events.csv"
OUT="evidence/v6.0/app_switching/ee2.txt"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "EE-2 RESULT=FAIL (no device/emulator)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "EE-2 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

T="$(adb shell toybox date +%F 2>/dev/null | tr -d '\r')"

adb shell run-as "$PKG" sh <<IN
set -eu
mkdir -p files
f="$CSV_IN"
echo "date,time,event_type,package" > "\$f"
echo "$T,10:00:00,FOREGROUND,com.test.a" >> "\$f"
echo "$T,10:05:00,FOREGROUND,com.test.b" >> "\$f"
echo "$T,10:10:00,FOREGROUND,com.test.a" >> "\$f"
IN

adb shell run-as "$PKG" rm -f "$CSV_OUT" >/dev/null 2>&1 || true
adb logcat -c >/dev/null 2>&1 || true

adb shell cmd activity broadcast -n "$RCV" -a "$ACT" --receiver-foreground --user 0 >/dev/null 2>&1 || true

deadline=$(( $(date +%s) + 30 ))
HDR=""
while :; do
HDR="$(adb exec-out run-as "$PKG" sed -n '1p' "$CSV_OUT" 2>/dev/null | tr -d '\r' || true)"
[ -n "$HDR" ] && break
[ "$(date +%s)" -ge "$deadline" ] && break
sleep 1
done

ROW_T="$(adb exec-out run-as "$PKG" awk -F, -v d="$T" 'NR>1&&$1==d{print;exit}' "$CSV_OUT" 2>/dev/null | tr -d '\r' || true)"

{
echo "=== HEADER ==="
echo "${HDR:-[missing]}"
echo
echo "=== TODAY ($T) ==="
[ -n "$ROW_T" ] && echo "$ROW_T" || echo "[none]"
} | tee "$OUT" >/dev/null

[ "$HDR" = "date,switches,entropy" ] || { echo "EE-2 RESULT=FAIL" | tee -a "$OUT"; exit 1; }
[ -n "$ROW_T" ] || { echo "EE-2 RESULT=FAIL" | tee -a "$OUT"; exit 1; }

SW="$(printf '%s\n' "$ROW_T" | awk -F, '{print $2}')"
EN="$(printf '%s\n' "$ROW_T" | awk -F, '{print $3}')"

printf '%s\n' "$SW" | grep -Eq '^[0-9]+$' || { echo "EE-2 RESULT=FAIL" | tee -a "$OUT"; exit 1; }
printf '%s\n' "$EN" | grep -Eq '^[0-9]+([.][0-9]+)?$' || { echo "EE-2 RESULT=FAIL" | tee -a "$OUT"; exit 1; }

echo "EE-2 RESULT=PASS" | tee -a "$OUT"
exit 0
