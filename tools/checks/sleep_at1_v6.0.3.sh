#!/bin/sh
PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/sleep/at1.3.txt"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "AT-1 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "AT-1 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

CSV_MAIN="$(adb exec-out run-as "$PKG" cat files/daily_sleep.csv 2>/dev/null | tr -d '\r')"
[ -n "$CSV_MAIN" ] || { echo "AT-1 RESULT=FAIL (missing daily_sleep.csv)" | tee "$OUT"; exit 4; }

HEAD_MAIN="$(printf '%s\n' "$CSV_MAIN" | head -n1)"
printf '%s\n' "$HEAD_MAIN" | grep -q '^date,' || { echo "AT-1 RESULT=FAIL (bad header)" | tee "$OUT"; exit 5; }
printf '%s\n' "$HEAD_MAIN" | grep -Eq '(duration_minutes|duration_hours)' || { echo "AT-1 RESULT=FAIL (missing duration column)" | tee "$OUT"; exit 6; }

HDR_LOCK="$(tr -d '\r' < app/locks/daily_sleep_duration.header 2>/dev/null || printf 'date,hours')"
CSV_DUR="$(adb exec-out run-as "$PKG" cat files/daily_sleep_duration.csv 2>/dev/null | tr -d '\r')"
[ -n "$CSV_DUR" ] || { echo "AT-1 RESULT=FAIL (missing daily_sleep_duration.csv)" | tee "$OUT"; exit 7; }
HEAD_DUR="$(printf '%s\n' "$CSV_DUR" | head -n1)"
[ "$HEAD_DUR" = "$HDR_LOCK" ] || { echo "AT-1 RESULT=FAIL (duration header drift)" | tee "$OUT"; exit 8; }

echo "AT-1 RESULT=PASS" | tee "$OUT"
exit 0
