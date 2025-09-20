#!/bin/bash
PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/sleep/di2.1.txt"

adb get-state >/dev/null 2>&1 || { echo "DI-2 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "DI-2 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

CSV="$(adb exec-out run-as "$PKG" sh -c 'cat files/daily_sleep.csv 2>/dev/null' | tr -d $'\r')"
[ -n "$CSV" ] || { echo "DI-2 RESULT=FAIL (missing csv)" | tee "$OUT"; exit 4; }

DUPS="$(awk -F, 'NR>1{c[$1]++} END{for(d in c) if(c[d]>1) print d":"c[d]}' <<<"$CSV")"

if [ -z "$DUPS" ]; then
echo "DI-2 RESULT=PASS" | tee "$OUT"
exit 0
else
{
echo "DI-2 RESULT=FAIL"
printf "%s\n" "$DUPS"
} | tee "$OUT"
exit 1
fi
