#!/usr/bin/env bash
PKG="com.nick.myrecoverytracker"
F="files/daily_late_screen.csv"

HDR="$(adb exec-out run-as "$PKG" head -n1 $F 2>/dev/null | tr -d '\r')"
if [ "$HDR" = "date,late_minutes" ]; then
  echo "LNS DI-1 RESULT=PASS"
  exit 0
else
  echo "LNS DI-1 RESULT=FAIL (got '$HDR')"
  exit 1
fi
