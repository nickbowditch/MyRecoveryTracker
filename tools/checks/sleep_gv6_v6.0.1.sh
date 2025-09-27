#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
CSV="files/daily_sleep_duration.csv"
LOCK="app/locks/daily_sleep_duration.header"
OUT="evidence/v6.0/sleep/gv6.1.txt"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "GV-6 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG"  >/dev/null 2>&1 || { echo "GV-6 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

EXP="$(tr -d '\r' < "$LOCK" 2>/dev/null || true)"
[ -n "$EXP" ] || { echo "GV-6 RESULT=FAIL (missing lock)" | tee "$OUT"; exit 4; }

HDR="$(adb exec-out run-as "$PKG" head -n1 "$CSV" 2>/dev/null | tr -d '\r' || true)"
[ -n "$HDR" ] || { echo "GV-6 RESULT=FAIL (missing csv)" | tee "$OUT"; exit 5; }
[ "$HDR" = "$EXP" ] || { echo "GV-6 RESULT=FAIL (header drift)" | tee "$OUT"; exit 6; }

adb exec-out run-as "$PKG" tail -n +2 "$CSV" 2>/dev/null | tr -d '\r' |
awk -F',' '
function is_num(x){ return x ~ /^-?[0-9]+([.][0-9]+)?$/ }
{ v=$2;
if(v=="") next;
if(!(is_num(v) && (v+0)>=0 && (v+0)<=18.0)) exit 1
}
END{ }
' || { echo "GV-6 RESULT=FAIL (type/range violation)" | tee "$OUT"; exit 7; }

echo "GV-6 RESULT=PASS" | tee "$OUT"
exit 0
