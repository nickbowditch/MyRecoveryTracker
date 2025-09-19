#!/bin/bash
PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/sleep/di1_fix.1.txt"

adb get-state >/dev/null 2>&1 || { echo "DI-1-FIX RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "DI-1-FIX RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

EXP="date,sleep_time,wake_time,duration_hours"
mkdir -p app/locks
if [ -f app/locks/daily_sleep.header ]; then
  cur="$(tr -d $'\r' < app/locks/daily_sleep.header 2>/dev/null)"
  if [ "$cur" = "$EXP" ]; then
    echo "DI-1-FIX RESULT=PASS (already sealed)" | tee "$OUT"; exit 0
  fi
fi
printf '%s\n' "$EXP" > app/locks/daily_sleep.header
echo "DI-1-FIX RESULT=PASS (sealed)" | tee "$OUT"; exit 0
