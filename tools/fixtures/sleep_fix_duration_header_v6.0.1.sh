#!/bin/bash
PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/sleep/di1_fix_duration.1.txt"
adb get-state >/dev/null 2>&1 || { echo "DI-1-FIX RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "DI-1-FIX RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }
EXP="date,hours"
mkdir -p app/locks
CUR="$(tr -d $'\r' < app/locks/daily_sleep_duration.header 2>/dev/null)"
[ "$CUR" = "$EXP" ] && { echo "DI-1-FIX RESULT=PASS (already sealed)" | tee "$OUT"; exit 0; }
printf '%s\n' "$EXP" > app/locks/daily_sleep_duration.header
echo "DI-1-FIX RESULT=PASS (sealed)" | tee "$OUT"; exit 0
