#!/usr/bin/env bash
PKG="com.nick.myrecoverytracker"
CSV="$(adb exec-out run-as "$PKG" cat files/daily_late_screen.csv 2>/dev/null || printf "")"
HDR="$(printf "%s\n" "$CSV" | head -n1)"
[ "$HDR" = "date,late_minutes" ] || { echo "LNS TC-1 RESULT=FAIL (bad header)"; exit 1; }

BAD="$(printf "%s\n" "$CSV" | awk -F, 'NR>1{print $1}' | while read -r d; do
  [ -z "$d" ] && continue
  rd="$(adb shell "toybox date -d '$d 12:00:00' +%F" | tr -d '\r')"
  [ "$rd" = "$d" ] || echo "$d"
done)"
if [ -z "$BAD" ]; then
  echo "LNS TC-1 RESULT=PASS"
  exit 0
else
  echo "LNS TC-1 RESULT=FAIL"
  printf "%s\n" "$BAD"
  exit 1
fi
