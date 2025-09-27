#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
CSV="files/daily_notification_engagement.csv"
LOCK="app/locks/daily_notif_engagement.header"
OUT="evidence/v6.0/notification_engagement/di3.6.txt"
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
function is_int(x){ return x ~ /^[0-9]+$/ }
function is_float(x){ return x ~ /^[0-9]*\.[0-9]+$/ || x ~ /^[0-9]+$/ }
function abs(v){ return v<0?-v:v }
{
  d=$1; fs=$2; del=$3; op=$4; rate=$5
  if(!date_ok(d)) exit 1
  if(!is_int(del) || del<0 || del>5000) exit 1
  if(!is_int(op) || op<0 || op>5000) exit 1
  if(op>del) exit 1
  if(rate!=""){
    if(!is_float(rate)) exit 1
    if(rate<0 || rate>1.0) exit 1
    exp=0; diff=0
    if(del>0){ exp=op/del }
    diff=rate-exp
    if(diff<0){ diff=-diff }
    if(diff>0.01) exit 1
  }
}
END{ }' || { echo "DI-3 RESULT=FAIL (invalid rows)" | tee "$OUT"; exit 7; }

echo "DI-3 RESULT=PASS" | tee "$OUT"
exit 0
