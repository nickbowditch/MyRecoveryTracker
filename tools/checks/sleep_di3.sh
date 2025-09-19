#!/bin/bash
PKG="com.nick.myrecoverytracker"
MIN="${MIN_HOURS:-0}"
MAX="${MAX_HOURS:-16}"
CSV="$(adb exec-out run-as "$PKG" cat files/daily_sleep_summary.csv 2>/dev/null || printf "")"
BAD="$(printf "%s\n" "$CSV" | awk -F, -v lo="$MIN" -v hi="$MAX" 'NR>1{
  v=$4; if (v=="" || v!~/^-?[0-9]+(\.[0-9]+)?$/) { print $0; next }
  x=v+0; if (x<lo || x>hi) print $0
}')"
if [ -z "$BAD" ]; then
  echo "Sleep DI-3 RESULT=PASS"
  exit 0
else
  echo "Sleep DI-3 RESULT=FAIL"
  printf "%s\n" "$BAD"
  exit 1
fi
