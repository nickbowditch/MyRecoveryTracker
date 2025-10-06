#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/app_switching/di3.txt"
CSV="files/daily_app_switching.csv"
LOCK="app/locks/daily_app_switching.header"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "DI-3 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "DI-3 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

EXP="$(tr -d '\r' < "$LOCK" 2>/dev/null || true)"
[ -n "$EXP" ] || { echo "DI-3 RESULT=FAIL (missing lock)" | tee "$OUT"; exit 4; }

HDR="$(adb exec-out run-as "$PKG" head -n1 "$CSV" 2>/dev/null | tr -d '\r' || true)"
[ -n "$HDR" ] || { echo "DI-3 RESULT=FAIL (missing csv)" | tee "$OUT"; exit 5; }
[ "$HDR" = "$EXP" ] || { echo "DI-3 RESULT=FAIL (header drift)" | tee "$OUT"; exit 6; }

adb exec-out run-as "$PKG" tail -n +2 "$CSV" 2>/dev/null | tr -d '\r' | awk -F',' '
function date_ok(d){ return d ~ /^[0-9]{4}-[0-9]{2}-[0-9]{2}$/ }
function int_ok(x){ return x ~ /^[0-9]+$/ }
function float_ok(x){ return x ~ /^([0-9]+(\.[0-9]+)?)$/ }
{
  d=$1; s=$2; e=$3
  if(!date_ok(d)){ bad++; next }
  if(!int_ok(s)){ bad++; next }
  s+=0
  if(s<0 || s>10000){ bad++; next }
  if(s==0 && e!="0" && e!=""){ bad++; next }
  if(e!="0" && e!=""){ 
    if(!float_ok(e)){ bad++; next }
    e+=0
    if(e<0 || e>3600000){ bad++; next }
  }
}
END{
  if(bad>0){ print "BAD_ROWS=" bad; exit 1 }
}' > /dev/null || {
  echo "DI-3 RESULT=FAIL (invalid types or out-of-range values)" | tee "$OUT"
  echo "--- DEBUG: HEAD ---" | tee -a "$OUT"
  adb exec-out run-as "$PKG" head -n 10 "$CSV" 2>/dev/null | tr -d '\r' | tee -a "$OUT"
  exit 7
}

echo "DI-3 RESULT=PASS" | tee "$OUT"
exit 0
