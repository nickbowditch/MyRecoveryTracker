#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/sleep/di3.10.txt"
CSV="files/daily_sleep.csv"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "DI-3 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG"  >/dev/null 2>&1 || { echo "DI-3 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

CSV_CONTENT="$(adb exec-out run-as "$PKG" cat "$CSV" 2>/dev/null | tr -d '\r' || true)"
[ -n "$CSV_CONTENT" ] || { echo "DI-3 RESULT=FAIL (missing csv)" | tee "$OUT"; exit 4; }

printf '%s\n' "$CSV_CONTENT" | awk -F',' '
function date_ok(d){ return d ~ /^[0-9]{4}-[0-9]{2}-[0-9]{2}$/ }
function time_ok(t){ return (t=="" || t ~ /^([01][0-9]|2[0-3]):[0-5][0-9](:[0-5][0-9])?$/) }
function is_int(x){ return x ~ /^-?[0-9]+$/ }
function is_num(x){ return x ~ /^-?[0-9]+([.][0-9]+)?$/ }
NR==1{
  for(i=1;i<=NF;i++){
    h[$i]=i
  }
  if(!h["date"]) exit 5
  if(!h["sleep_time"] || !h["wake_time"]) exit 5
  if(!h["duration_minutes"] && !h["duration_hours"]) exit 5
  next
}
NR>1{
  d=$(h["date"]? $h["date"] : "")
  st=$(h["sleep_time"]? $h["sleep_time"] : "")
  wt=$(h["wake_time"]? $h["wake_time"] : "")
  vmin=(h["duration_minutes"]? $h["duration_minutes"] : "")
  vhr =(h["duration_hours"]?   $h["duration_hours"]   : "")
  if(!date_ok(d)) { bad=1; exit }
  if(!time_ok(st) || !time_ok(wt)) { bad=1; exit }
  ok=0
  if(vmin!=""){
    if(is_int(vmin) && (vmin+0)>=0 && (vmin+0)<=1440) ok=1
    else { bad=1; exit }
  }
  if(vhr!=""){
    if(is_num(vhr) && (vhr+0)>=0 && (vhr+0)<=24) ok=1
    else { bad=1; exit }
  }
  if(!ok){ bad=1; exit }
}
END{
  if(bad) exit 1
}
' || { echo "DI-3 RESULT=FAIL" | tee "$OUT"; exit 1; }

echo "DI-3 RESULT=PASS" | tee "$OUT"
exit 0
