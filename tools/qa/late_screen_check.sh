#!/bin/bash
PKG="com.nick.myrecoverytracker"
F="files/daily_late_screen.csv"
HDR="$(adb exec-out run-as "$PKG" head -n1 "$F" 2>/dev/null | tr -d $'\r')"
[ -n "$HDR" ] || { echo "FAIL: files/daily_late_screen.csv missing or empty"; exit 1; }
REQ="date,late_night_screen_minutes,feature_schema_version"
if [ "$HDR" != "$REQ" ]; then
  echo "FAIL: header mismatch"
  echo "expected: $REQ"
  echo "actual:   $HDR"
  exit 1
fi
DUPES="$(adb exec-out run-as "$PKG" awk -F, 'NR>1{c[$1]++} END{for(k in c) if(c[k]>1) print k}' "$F")"
[ -z "$DUPES" ] || { echo "FAIL: duplicate date rows:"; echo "$DUPES"; exit 1; }
BAD="$(adb exec-out run-as "$PKG" awk -F, 'NR>1 && $1 !~ /^[0-9]{4}-[0-9]{2}-[0-9]{2}$/ {print $1}' "$F")"
[ -z "$BAD" ] || { echo "FAIL: bad date format(s):"; echo "$BAD"; exit 1; }
adb exec-out run-as "$PKG" awk -F, 'NR==2{prev=$1} NR>2{if($1<prev){print "FAIL: dates not sorted: " prev " -> " $1; exit 2} prev=$1} NR>1&&($2+0)<0{print "FAIL: negative minutes on " $1; exit 3} NR>1&&($2+0)>1440{print "FAIL: minutes > 1440 on " $1; exit 4} END{print "OK: daily_late_screen.csv header/dupes/range"}' "$F"
