#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
CSV_PATH="files/daily_sleep_summary.csv"
OUT="evidence/v6.0/sleep/tc1.8.txt"
EXP_HDR="date,sleep_time,wake_time,duration_hours"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "TC-1 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG"  >/dev/null 2>&1 || { echo "TC-1 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

HDR="$(adb exec-out run-as "$PKG" head -n1 "$CSV_PATH" 2>/dev/null | tr -d '\r' || true)"
[ -n "$HDR" ] || { echo "TC-1 RESULT=FAIL (missing csv)" | tee "$OUT"; exit 4; }
[ "$HDR" = "$EXP_HDR" ] || { echo "TC-1 RESULT=FAIL (bad header: $HDR)" | tee "$OUT"; exit 5; }

TODAY="$(adb shell date +%F 2>/dev/null | tr -d '\r')"
if adb exec-out run-as "$PKG" awk -F, -v t="$TODAY" 'NR>1 && $1>t{exit 1} END{exit 0}' "$CSV_PATH"; then
echo "TC-1 RESULT=PASS" | tee "$OUT"; exit 0
else
echo "TC-1 RESULT=FAIL (future-dated rows)" | tee "$OUT"; exit 6
fi
