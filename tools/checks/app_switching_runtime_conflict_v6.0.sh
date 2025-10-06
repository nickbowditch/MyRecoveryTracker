#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
CSV="files/daily_app_switching.csv"
RCV="$PKG/.TriggerReceiver"
ACT_SWITCH="$PKG.ACTION_RUN_APP_SWITCHING_DAILY"
ACT_USAGE="$PKG.ACTION_RUN_USAGE_CAPTURE"
OUT="evidence/v6.0/app_switching/runtime_conflict.txt"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "RUNTIME RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "RUNTIME RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

HDR0="$(adb exec-out run-as "$PKG" sed -n '1p' "$CSV" 2>/dev/null | tr -d '\r' || true)"

adb logcat -c >/dev/null 2>&1 || true
adb shell run-as "$PKG" rm -f "$CSV" >/dev/null 2>&1 || true
adb shell cmd activity broadcast -n "$RCV" -a "$ACT_SWITCH" --receiver-foreground --user 0 >/dev/null 2>&1 || true
sleep 2
HDR_SWITCH="$(adb exec-out run-as "$PKG" sed -n '1p' "$CSV" 2>/dev/null | tr -d '\r' || true)"

adb shell cmd activity broadcast -n "$RCV" -a "$ACT_USAGE" --receiver-foreground --user 0 >/dev/null 2>&1 || true
sleep 2
HDR_USAGE="$(adb exec-out run-as "$PKG" sed -n '1p' "$CSV" 2>/dev/null | tr -d '\r' || true)"

{
  echo "before=${HDR0:-[missing]}"
  echo "after_switch=${HDR_SWITCH:-[missing]}"
  echo "after_usage=${HDR_USAGE:-[missing]}"
} | tee "$OUT" >/dev/null

if [ "${HDR_SWITCH:-}" = "date,switches,entropy" ] && [ "${HDR_USAGE:-}" = "date,package,starts" ]; then
  echo "RUNTIME RESULT=FAIL (same file used by two schemas; UsageCapture overwrites rollup)" | tee -a "$OUT"
  exit 1
fi

if [ -z "${HDR_SWITCH:-}" ]; then
  echo "RUNTIME RESULT=FAIL (switching trigger did not write $CSV)" | tee -a "$OUT"
  exit 1
fi

if [ "${HDR_SWITCH:-}" = "date,switches,entropy" ] && [ "${HDR_USAGE:-}" = "date,switches,entropy" ]; then
  echo "RUNTIME RESULT=PASS (single schema present)" | tee -a "$OUT"
  exit 0
fi

echo "RUNTIME RESULT=FAIL (unexpected headers; investigate writers)" | tee -a "$OUT"
exit 1
