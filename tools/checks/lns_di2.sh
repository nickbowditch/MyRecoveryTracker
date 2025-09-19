#!/usr/bin/env bash
PKG="com.nick.myrecoverytracker"
F="files/daily_late_screen.csv"

DUPS="$(adb exec-out run-as "$PKG" awk -F, 'NR>1{c[$1]++} END{for(d in c) if(c[d]>1) print d}' $F 2>/dev/null)"
if [ -z "$DUPS" ]; then
  echo "LNS DI-2 RESULT=PASS"
  exit 0
else
  echo "LNS DI-2 RESULT=FAIL (dupes: $DUPS)"
  exit 1
fi
