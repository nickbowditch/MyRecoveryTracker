#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/sleep/at1.4.txt"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "AT-1 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "AT-1 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

S_SUM="$(adb exec-out run-as "$PKG" cat files/daily_sleep_summary.csv 2>/dev/null | tr -d '\r' || true)"
S_DUR="$(adb exec-out run-as "$PKG" cat files/daily_sleep_duration.csv 2>/dev/null | tr -d '\r' || true)"
[ -n "$S_SUM" ] || { echo "AT-1 RESULT=FAIL (missing daily_sleep_summary.csv)" | tee "$OUT"; exit 4; }
[ -n "$S_DUR" ] || { echo "AT-1 RESULT=FAIL (missing daily_sleep_duration.csv)" | tee "$OUT"; exit 5; }

HDR_SUM="$(printf '%s\n' "$S_SUM" | head -n1 | sed -e 's/^\xEF\xBB\xBF//')"
HDR_DUR="$(printf '%s\n' "$S_DUR" | head -n1 | sed -e 's/^\xEF\xBB\xBF//')"

[ "$HDR_SUM" = "date,sleep_time,wake_time,duration_hours" ] || { echo "AT-1 RESULT=FAIL (summary header='$HDR_SUM')" | tee "$OUT"; exit 6; }
[ "$HDR_DUR" = "date,duration_hours" ] || { echo "AT-1 RESULT=FAIL (duration header='$HDR_DUR')" | tee "$OUT"; exit 7; }

echo "AT-1 RESULT=PASS" | tee "$OUT"
exit 0
