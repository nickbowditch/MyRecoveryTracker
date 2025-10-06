#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/app_usage_by_category/di3.1.txt"
CSV="files/app_category_daily.csv"
LOCK="app/locks/app_category_daily.header"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "DI-3 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG"  >/dev/null 2>&1 || { echo "DI-3 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

EXP="$(tr -d '\r' < "$LOCK" 2>/dev/null || true)"
[ -n "$EXP" ] || { echo "DI-3 RESULT=FAIL (missing lock)" | tee "$OUT"; exit 4; }

HDR="$(adb exec-out run-as "$PKG" head -n1 "$CSV" 2>/dev/null | tr -d '\r' || true)"
[ -n "$HDR" ] || { echo "DI-3 RESULT=FAIL (missing csv)" | tee "$OUT"; exit 5; }
[ "$HDR" = "$EXP" ] || { echo "DI-3 RESULT=FAIL (header drift)" | tee "$OUT"; exit 6; }

adb exec-out run-as "$PKG" tail -n +2 "$CSV" 2>/dev/null | tr -d '\r' | awk -F',' '
function date_ok(d){ return d ~ /^[0-9]{4}-[0-9]{2}-[0-9]{2}$/ }
function num_ok(x){ return x ~ /^[0-9]+(\.[0-9]+)?$/ }
{
  d=$1; c=$2; m=$3
  if(!date_ok(d)) exit 1
  if(c=="") exit 1
  if(!num_ok(m)) exit 1
  m+=0
  if(m<0 || m>1440) exit 1
}
END{}' || { echo "DI-3 RESULT=FAIL (invalid rows or out of range minutes)" | tee "$OUT"; exit 7; }

echo "DI-3 RESULT=PASS" | tee "$OUT"
exit 0
