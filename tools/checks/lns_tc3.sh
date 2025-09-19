#!/usr/bin/env bash
PKG="com.nick.myrecoverytracker"
CSV="$(adb exec-out run-as "$PKG" cat files/daily_late_screen.csv 2>/dev/null || printf "")"
TODAY="$(adb shell 'date +%F' | tr -d '\r')"

BAD="$(printf "%s\n" "$CSV" | awk -F, 'NR>1{print $1}' | while read -r d; do
  [ -z "$d" ] && continue
  de="$(adb shell "toybox date -d '$d 12:00:00' +%s" | tr -d '\r')"
  te="$(adb shell "toybox date -d '$TODAY 12:00:00' +%s" | tr -d '\r')"
  [ -z "$de" ] && continue
  [ "$de" -le "$te" ] || echo "$d"
done)"
if [ -z "$BAD" ]; then
  echo "LNS TC-3 RESULT=PASS"
  exit 0
else
  echo "LNS TC-3 RESULT=FAIL"
  printf "%s\n" "$BAD"
  exit 1
fi
