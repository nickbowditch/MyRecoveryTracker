#!/bin/bash

PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/unlocks/ee5.txt"
CSV="/data/data/$PKG/files/daily_usage_events.csv"
S=0

adb get-state >/dev/null 2>&1 || { echo "EE-5 RESULT: FAIL (no device/emulator)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "EE-5 RESULT: FAIL (app not installed)" | tee "$OUT"; exit 3; }

adb shell run-as "$PKG" ls "$CSV" >/dev/null 2>&1 || S=1

CONTENT="$(adb shell run-as "$PKG" cat "$CSV" 2>/dev/null)"
[ -n "$CONTENT" ] || S=1

VALID_ROWS=$(printf "%s\n" "$CONTENT" | awk -F',' '
$1 ~ /^[0-9]{4}-[0-9]{2}-[0-9]{2}$/ && $2 ~ /^[0-9]+$/ {c++}
END {print c+0}
')

[ "$VALID_ROWS" -ge 1 ] || S=1

SCREEN_UNLOCKS=$(printf "%s\n" "$CONTENT" | awk -F',' 'END{print $2+0}')
TOTAL_UNLOCKS=$(printf "%s\n" "$CONTENT" | awk -F',' '{s+=$2} END{print s+0}')

echo "EE-5 DATA valid_rows=$VALID_ROWS" | tee "$OUT"
echo "EE-5 DATA screen_unlocks_per_day=$SCREEN_UNLOCKS" | tee -a "$OUT"
echo "EE-5 DATA total_unlocks=$TOTAL_UNLOCKS" | tee -a "$OUT"

if [ "$S" -eq 0 ]; then
  echo "EE-5 RESULT: PASS" | tee -a "$OUT"
  exit 0
else
  echo "EE-5 RESULT: FAIL" | tee -a "$OUT"
  exit 1
fi
