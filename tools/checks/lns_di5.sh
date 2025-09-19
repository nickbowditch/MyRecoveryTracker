#!/usr/bin/env bash
PKG="com.nick.myrecoverytracker"
ROWS="$(adb exec-out run-as "$PKG" sh -c 'wc -l < files/daily_late_screen.csv 2>/dev/null || echo 0' | tr -d '\r')"
if [ "$ROWS" -gt 1 ]; then
  echo "LNS DI-5 RESULT=PASS ($ROWS lines)"
  exit 0
else
  echo "LNS DI-5 RESULT=FAIL (empty)"
  exit 1
fi
