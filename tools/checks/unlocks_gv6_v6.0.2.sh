#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
CSV="files/daily_unlocks.csv"
LOCK="app/locks/daily_unlocks.header"
OUT="evidence/v6.0/unlocks/gv6.2.txt"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "GV-6 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "GV-6 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

EXP="$(tr -d '\r' < "$LOCK" 2>/dev/null || true)"
[ -n "$EXP" ] || { echo "GV-6 RESULT=FAIL (missing lock)" | tee "$OUT"; exit 4; }

HDR="$(adb exec-out run-as "$PKG" head -n1 "$CSV" 2>/dev/null | tr -d '\r' || true)"
[ -n "$HDR" ] || { echo "GV-6 RESULT=FAIL (missing csv)" | tee "$OUT"; exit 5; }

[ "$HDR" = "$EXP" ] || { echo "GV-6 RESULT=FAIL (header drift)" | tee "$OUT"; exit 6; }

adb exec-out run-as "$PKG" tail -n +2 "$CSV" 2>/dev/null | tr -d '\r' |
awk -F',' '
BEGIN{bad=0}
NR==1{next}
{
d=$1; sv=$2; du=$3
if(sv!="v6.0"){ bad=1; exit }
if(du !~ /^[0-9]+$/){ bad=1; exit }
if((du+0)<0 || (du+0)>20000){ bad=1; exit }
}
END{ exit bad }
' || { echo "GV-6 RESULT=FAIL (type/range violation)" | tee "$OUT"; exit 7; }

echo "GV-6 RESULT=PASS" | tee "$OUT"
exit 0
