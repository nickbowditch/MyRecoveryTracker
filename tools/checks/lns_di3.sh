#!/usr/bin/env bash
PKG="com.nick.myrecoverytracker"
F="files/daily_late_screen.csv"

BAD="$(adb exec-out run-as "$PKG" awk -F, 'NR>1 && ($2<0 || $2>1440 || $2~/[^0-9]/){print $1":"$2}' $F 2>/dev/null)"
if [ -z "$BAD" ]; then
  echo "LNS DI-3 RESULT=PASS"
  exit 0
else
  echo "LNS DI-3 RESULT=FAIL (bad rows: $BAD)"
  exit 1
fi
