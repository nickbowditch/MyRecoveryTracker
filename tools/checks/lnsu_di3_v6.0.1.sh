#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/lnsu/di3.1.txt"
CSV="files/daily_lnsu.csv"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "DI-3 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "DI-3 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

HDR="$(adb exec-out run-as "$PKG" head -n1 "$CSV" 2>/dev/null | tr -d '\r' || true)"
[ -n "$HDR" ] || { echo "DI-3 RESULT=FAIL (missing csv)" | tee "$OUT"; exit 4; }

adb exec-out run-as "$PKG" tail -n +2 "$CSV" 2>/dev/null | tr -d '\r' | awk -F',' '
function date_ok(d){ return d ~ /^[0-9]{4}-[0-9]{2}-[0-9]{2}$/ }
function is_int(x){ return x ~ /^-?[0-9]+$/ }
{
  d=$1; ver=$2; mins=$3
  if(!date_ok(d)) exit 1
  if(ver!="v6.0") exit 1
  if(mins=="") exit 1
  if(!is_int(mins)) exit 1
  if((mins+0)<0 || (mins+0)>240) exit 1
}
END{ }' || { echo "DI-3 RESULT=FAIL (invalid rows)" | tee "$OUT"; exit 5; }

echo "DI-3 RESULT=PASS" | tee "$OUT"
exit 0
