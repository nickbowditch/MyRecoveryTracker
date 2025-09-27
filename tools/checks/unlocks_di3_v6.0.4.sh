#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
CSV="files/daily_unlocks.csv"
OUT="evidence/v6.0/unlocks/di3.4.txt"
mkdir -p "$(dirname "$OUT")"
adb get-state >/dev/null 2>&1 || { echo "DI-3 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG"  >/dev/null 2>&1 || { echo "DI-3 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }
DATA="$(adb exec-out run-as "$PKG" cat "$CSV" 2>/dev/null | tr -d '\r' || true)"
[ -n "$DATA" ] || { echo "DI-3 RESULT=FAIL (missing csv)" | tee "$OUT"; exit 4; }
printf '%s\n' "$DATA" | awk -F',' '
NR==1{
for(i=1;i<=NF;i++) if($i=="daily_unlocks") c=i
if(!c) exit 5
next
}
NR>1{
v=$c
if(v !~ /^[0-9]+$/) exit 1
if((v+0)<0 || (v+0)>20000) exit 1
}
END{ if(!c) exit 5 }
' || { rc=$?; case "$rc" in 5) echo "DI-3 RESULT=FAIL (missing daily_unlocks column)" | tee "$OUT";; *) echo "DI-3 RESULT=FAIL (type/range violation)" | tee "$OUT";; esac; exit 1; }
echo "DI-3 RESULT=PASS" | tee "$OUT"
exit 0
