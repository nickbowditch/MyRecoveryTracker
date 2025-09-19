#!/bin/bash
PKG="com.nick.myrecoverytracker"
CSV="$(adb exec-out run-as "$PKG" cat files/daily_sleep.csv 2>/dev/null || printf "")"
[ -n "$CSV" ] || { echo "TC-5 RESULT=FAIL (missing csv)"; exit 2; }
dates="$(printf '%s\n' "$CSV" | awk -F, 'NR>1{print $1}')"
prev=""
while IFS= read -r d; do
  [ -z "$prev" ] && { prev="$d"; continue; }
  s=$(adb shell toybox date -d "$d" +%s | tr -d '\r')
  p=$(adb shell toybox date -d "$prev" +%s | tr -d '\r')
  [ -n "$s" ] && [ -n "$p" ] || { echo "TC-5 RESULT=FAIL"; exit 1; }
  diff=$(( (s - p) / 86400 ))
  if [ "$diff" -gt 2 ]; then
    echo "TC-5 RESULT=FAIL"
    echo "$prev->$d ($diff days)"
    exit 1
  fi
  prev="$d"
done <<< "$dates"
echo "TC-5 RESULT=PASS"
exit 0
