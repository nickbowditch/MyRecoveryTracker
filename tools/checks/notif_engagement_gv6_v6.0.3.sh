#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
CSV="files/daily_notification_engagement.csv"
LOCK="app/locks/daily_notif_engagement.header"
OUT="evidence/v6.0/notification_engagement/gv6.3.txt"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "GV-6 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG"  >/dev/null 2>&1 || { echo "GV-6 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

EXP="$(tr -d '\r' < "$LOCK" 2>/dev/null || true)"
[ -n "$EXP" ] || { echo "GV-6 RESULT=FAIL (missing lock)" | tee "$OUT"; exit 4; }

HDR="$(adb exec-out run-as "$PKG" head -n1 "$CSV" 2>/dev/null | tr -d '\r' || true)"
[ -n "$HDR" ] || { echo "GV-6 RESULT=FAIL (missing csv)" | tee "$OUT"; exit 5; }
[ "$HDR" = "$EXP" ] || { echo "GV-6 RESULT=FAIL (header drift)" | tee "$OUT"; exit 6; }

if adb exec-out run-as "$PKG" tail -n +2 "$CSV" 2>/dev/null | tr -d '\r' | awk -F',' '
function date_ok(d){ return d ~ /^[0-9]{4}-[0-9]{2}-[0-9]{2}$/ }
function is_int(x){ return x ~ /^[0-9]+$/ }
function is_float(x){ return x ~ /^[0-9]+(\.[0-9]+)?$/ }
BEGIN{bad=0}
{
d=$1; fs=$2; del=$3; op=$4; rate=$5
if(!date_ok(d)) { bad=1; exit }
if(!is_int(del)) { bad=1; exit }
if(!is_int(op))  { bad=1; exit }
if((del+0)<0 || (del+0)>5000) { bad=1; exit }
if((op+0)<0  || (op+0)>5000)  { bad=1; exit }
if((op+0)>(del+0)) { bad=1; exit }
if(rate!=""){
if(!is_float(rate)) { bad=1; exit }
if((rate+0)<0 || (rate+0)>1.0) { bad=1; exit }
er=0
if((del+0)>0){ er=(op+0)/(del+0) }
diff=(rate+0)-er
if(diff<0){ diff=-diff }
if(diff>0.01){ bad=1; exit }
}
}
END{ exit bad?1:0 }'; then
echo "GV-6 RESULT=PASS" | tee "$OUT"; exit 0
else
echo "GV-6 RESULT=FAIL (type/range violation)" | tee "$OUT"; exit 7
fi
