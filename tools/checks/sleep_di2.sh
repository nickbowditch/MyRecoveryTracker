#!/bin/bash
PKG="com.nick.myrecoverytracker"
CSV="$(adb exec-out run-as "$PKG" cat files/daily_sleep_summary.csv 2>/dev/null || printf "")"
DUPS="$(printf "%s\n" "$CSV" | awk -F, 'NR>1{c[$1]++} END{for(d in c) if(c[d]>1) print d":"c[d]}')"
if [ -z "$DUPS" ]; then
  echo "Sleep DI-2 RESULT=PASS"
  exit 0
else
  echo "Sleep DI-2 RESULT=FAIL"
  printf "%s\n" "$DUPS"
  exit 1
fi
