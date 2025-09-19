#!/bin/bash
PKG="com.nick.myrecoverytracker"

TODAY="$(adb shell date +%F | tr -d $'\r')"
CSV="$(adb exec-out run-as "$PKG" cat files/daily_sleep.csv 2>/dev/null || printf "")"

BAD="$(awk -F, -v t="$TODAY" 'NR>1 && $1>t' <<<"$CSV")"

if [ -z "$BAD" ]; then
  echo "TC-3 RESULT=PASS"; exit 0
else
  echo "TC-3 RESULT=FAIL"
  printf "%s\n" "$BAD"
  exit 1
fi
