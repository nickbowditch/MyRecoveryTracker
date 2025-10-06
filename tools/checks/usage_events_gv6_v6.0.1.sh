#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
CSV="files/daily_usage_events.csv"
LOCK="app/locks/daily_usage_events.header"
OUT="evidence/v6.0/usage_events_daily/gv6.1.txt"
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
d=$1; cnt=$2
if(!date_ok(d)) { bad=1; exit }
if(!is_int(cnt)) { bad=1; exit }
cnt+=0
if(cnt<0 || cnt>500000){ bad=1; exit }
}
END{ exit bad?1:0 }'; then
echo "GV-6 RESULT=PASS" | tee "$OUT"; exit 0
else
echo "GV-6 RESULT=FAIL (type/range violation)" | tee "$OUT"; exit 7
fi
