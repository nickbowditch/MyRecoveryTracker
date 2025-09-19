#!/bin/bash
PKG="com.nick.myrecoverytracker"
adb get-state >/dev/null 2>&1 || { echo "DI-2 RESULT=FAIL (no device)"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "DI-2 RESULT=FAIL (app not installed)"; exit 3; }
CSV="$(adb exec-out run-as "$PKG" sh -c 'cat files/daily_unlocks.csv 2>/dev/null' | tr -d '\r')"
[ -n "$CSV" ] || { echo "DI-2 RESULT=FAIL (missing csv)"; exit 4; }
DUPS="$(awk -F, 'NR>1{c[$1]++} END{for(k in c) if(c[k]>1) print k":"c[k]}' <<<"$CSV")"
if [ -z "$DUPS" ]; then
echo "DI-2 RESULT=PASS"
exit 0
else
echo "DI-2 RESULT=FAIL"
printf "%s\n" "$DUPS"
exit 1
fi
