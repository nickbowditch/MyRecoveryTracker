#!/bin/sh
PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/sleep/di3.9.txt"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "DI-3 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG"  >/dev/null 2>&1 || { echo "DI-3 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

CSV="$(adb exec-out run-as "$PKG" cat files/daily_sleep.csv 2>/dev/null | tr -d '\r')"
[ -n "$CSV" ] || { echo "DI-3 RESULT=FAIL (missing csv)" | tee "$OUT"; exit 4; }

printf '%s\n' "$CSV" | awk -F',' '
function date_ok(d){ return d ~ /^[0-9]{4}-[0-9]{2}-[0-9]{2}$/ }
function time_ok(t){ return (t=="" || t ~ /^([01][0-9]|2[0-3]):[0-5]0-9?$/) }
function is_int(x){ return x ~ /^-?[0-9]+$/ }
function is_num(x){ return x ~ /^-?[0-9]+([.][0-9]+)?$/ }
NR==1{
for(i=1;i<=NF;i++){
if($i=="date") di=i
if($i=="sleep_time") si=i
if($i=="wake_time")  wi=i
if($i=="duration_minutes") mi=i
if($i=="duration_hours")   hi=i
}
if(!di||!si||!wi||(!mi&&!hi)) exit 5
next
}
NR>1{
d=$di; st=$si; wt=$wi; vmin=(mi? $mi:""); vhr=(hi? $hi:"")
if(!date_ok(d)) { bad=1; exit }
if(!time_ok(st) || !time_ok(wt)) { bad=1; exit }
if(vmin!=""){
if(!is_int(vmin) || (vmin+0)<0 || (vmin+0)>1440) { bad=1; exit }
} else if(vhr!=""){
if(!is_num(vhr) || (vhr+0)<0 || (vhr+0)>24) { bad=1; exit }
}
}
END{ if(bad){ exit 1 } }
' || { echo "DI-3 RESULT=FAIL" | tee "$OUT"; exit 1; }

echo "DI-3 RESULT=PASS" | tee "$OUT"
exit 0
