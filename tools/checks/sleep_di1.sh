#!/bin/bash
PKG="com.nick.myrecoverytracker"
CSV="$(adb exec-out run-as "$PKG" cat files/daily_sleep_summary.csv 2>/dev/null || printf "")"
EXP="date,sleep_time,wake_time,duration_hours"
HDR="$(printf "%s\n" "$CSV" | head -n1)"
if [ "$HDR" = "$EXP" ]; then
  echo "Sleep DI-1 RESULT=PASS"
  exit 0
else
  echo "Sleep DI-1 RESULT=FAIL (got='$HDR')"
  exit 1
fi
