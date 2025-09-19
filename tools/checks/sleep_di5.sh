#!/bin/bash
PKG="com.nick.myrecoverytracker"
ROWS="$(adb exec-out run-as "$PKG" sh -c 'wc -l < files/daily_sleep_summary.csv 2>/dev/null' | tr -d '\r')"
[ -z "$ROWS" ] && ROWS=0
if [ "$ROWS" -gt 1 ]; then
  echo "Sleep DI-5 RESULT=PASS"
  exit 0
else
  echo "Sleep DI-5 RESULT=FAIL"
  exit 1
fi
