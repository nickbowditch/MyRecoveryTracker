#!/bin/sh
set -eu

PKG="com.nick.myrecoverytracker"
RCV="$PKG/.TriggerReceiver"
ACT_SWITCH="$PKG.ACTION_RUN_APP_SWITCHING_DAILY"
ACT_USAGE="$PKG.ACTION_RUN_USAGE_CAPTURE"
CSV_SWITCH="files/daily_app_switching.csv"
CSV_EVENTS="files/usage_events.csv"
OUT="evidence/v6.0/app_switching/ee2.txt"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "EE-2 RESULT=FAIL (no device/emulator)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "EE-2 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

adb shell run-as "$PKG" rm -f "$CSV_SWITCH" >/dev/null 2>&1 || true
adb logcat -c >/dev/null 2>&1 || true

adb shell cmd activity broadcast -n "$RCV" -a "$ACT_USAGE" --receiver-foreground --user 0 >/dev/null 2>&1 || true

deadline_events=$(( $(date +%s) + 25 ))
HDR_EVENTS=""
while :; do
  HDR_EVENTS="$(adb exec-out run-as "$PKG" sed -n '1p' "$CSV_EVENTS" 2>/dev/null | tr -d '\r' || true)"
  [ -n "$HDR_EVENTS" ] && break
  [ "$(date +%s)" -ge "$deadline_events" ] && break
  sleep 1
done

adb shell cmd activity broadcast -n "$RCV" -a "$ACT_SWITCH" --receiver-foreground --user 0 >/dev/null 2>&1 || true

deadline_switch=$(( $(date +%s) + 25 ))
HDR_SWITCH=""
while :; do
  HDR_SWITCH="$(adb exec-out run-as "$PKG" sed -n '1p' "$CSV_SWITCH" 2>/dev/null | tr -d '\r' || true)"
  [ -n "$HDR_SWITCH" ] && break
  [ "$(date +%s)" -ge "$deadline_switch" ] && break
  sleep 1
done

T="$(adb shell toybox date +%F | tr -d '\r')"
Y="$(adb shell toybox date -d "@$(( $(adb shell toybox date +%s)-86400 ))" +%F | tr -d '\r')"
ROW_T="$(adb exec-out run-as "$PKG" awk -F, -v d="$T" 'NR>1&&$1==d{print;exit}' "$CSV_SWITCH" 2>/dev/null | tr -d '\r' || true)"
ROW_Y="$(adb exec-out run-as "$PKG" awk -F, -v d="$Y" 'NR>1&&$1==d{print;exit}' "$CSV_SWITCH" 2>/dev/null | tr -d '\r' || true)"

LOGS="$(adb logcat -d 2>/dev/null | grep -iE 'AppSwitchingDaily|UsageCapture|TriggerReceiver|WorkManager|enqueue|succeed|success|completed|finished' | tail -n 120 || true)"

{
echo "=== usage_events.csv HEADER ==="
echo "${HDR_EVENTS:-[missing]}"
echo
echo "=== daily_app_switching.csv HEADER ==="
echo "${HDR_SWITCH:-[missing]}"
echo
echo "=== TODAY ($T) ==="
[ -n "$ROW_T" ] && echo "$ROW_T" || echo "[none]"
echo
echo "=== YESTERDAY ($Y) ==="
[ -n "$ROW_Y" ] && echo "$ROW_Y" || echo "[none]"
echo
echo "=== LOGCAT ==="
[ -n "$LOGS" ] && echo "$LOGS" || echo "[none]"
} | tee "$OUT" >/dev/null

[ "$HDR_SWITCH" = "date,switches,entropy" ] || { echo "EE-2 RESULT=FAIL" | tee -a "$OUT"; exit 1; }
echo "EE-2 RESULT=PASS" | tee -a "$OUT"; exit 0
