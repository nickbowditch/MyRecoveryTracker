#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
CSV="files/daily_notification_latency.csv"
LOCK="app/locks/daily_notif_latency.header"
OUT="evidence/v6.0/notification_latency/gv6.3.txt"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "GV-6 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "GV-6 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

EXP="$(tr -d '\r' < "$LOCK" 2>/dev/null || true)"
[ -n "$EXP" ] || { echo "GV-6 RESULT=FAIL (missing lock)" | tee "$OUT"; exit 4; }

HDR="$(adb exec-out run-as "$PKG" head -n1 "$CSV" 2>/dev/null | tr -d '\r' || true)"
[ -n "$HDR" ] || { echo "GV-6 RESULT=FAIL (missing csv)" | tee "$OUT"; exit 5; }
[ "$HDR" = "$EXP" ] || { echo "GV-6 RESULT=FAIL (header drift)" | tee "$OUT"; exit 6; }

if adb exec-out run-as "$PKG" tail -n +2 "$CSV" 2>/dev/null | tr -d '\r' | awk -F',' '
function date_ok(d){ return d ~ /^[0-9]{4}-[0-9]{2}-[0-9]{2}$/ }
function is_int(x){ return x ~ /^[0-9]+$/ }
BEGIN{bad=0}
{
d=$1; fs=$2; p50=$3; p90=$4; p99=$5; cnt=$6
if(!date_ok(d)) { bad=1; exit }
if(!is_int(cnt)) { bad=1; exit }
cnt+=0
if(cnt<0 || cnt>5000){ bad=1; exit }

if(cnt==0){
ok = ((p50=="" || p50=="0") && (p90=="" || p90=="0") && (p99=="" || p99=="0"))
if(!ok){ bad=1; exit }
} else {
if(!is_int(p50) || !is_int(p90) || !is_int(p99)) { bad=1; exit }
p50+=0; p90+=0; p99+=0
if(p50<0 || p50>3600000) { bad=1; exit }
if(p90<0 || p90>3600000) { bad=1; exit }
if(p99<0 || p99>3600000) { bad=1; exit }
if(!(p50<=p90 && p90<=p99)) { bad=1; exit }
}
}
END{ exit bad?1:0 }'; then
echo "GV-6 RESULT=PASS" | tee "$OUT"; exit 0
else
echo "GV-6 RESULT=FAIL (type/range violation)" | tee "$OUT"; exit 7
fi
