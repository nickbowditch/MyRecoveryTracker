#!/bin/bash
PKG="com.nick.myrecoverytracker"
adb get-state >/dev/null 2>&1 || { echo "DI-3 RESULT=FAIL (no device)"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "DI-3 RESULT=FAIL (app not installed)"; exit 3; }
CSV="$(adb exec-out run-as "$PKG" sh -c 'cat files/daily_unlocks.csv 2>/dev/null' | tr -d '\r')"
[ -n "$CSV" ] || { echo "DI-3 RESULT=FAIL (missing csv)"; exit 4; }
BAD="$(awk -F, 'NR>1{ if($2 !~ /^[0-9]+$/) b=1; else if($2<0 || $2>1440) b=1; if(b){print $0; b=0} }' <<<"$CSV")"
if [ -z "$BAD" ]; then
  echo "DI-3 RESULT=PASS"
  exit 0
else
  echo "DI-3 RESULT=FAIL"
  printf "%s\n" "$BAD"
  exit 1
fi
