#!/bin/bash
PKG="com.nick.myrecoverytracker"

CSV="$(adb exec-out run-as "$PKG" cat files/daily_sleep.csv 2>/dev/null || printf "")"
[ -n "$CSV" ] || { echo "TC-5 RESULT=FAIL (missing csv)"; exit 2; }

GAP="$(
awk -F, '
NR==1{prev=""; next}
{d=$1; if(prev!=""){ cmd="date -d \"" d "\" +%s"; cmd | getline s; close(cmd);
cmd="date -d \"" prev "\" +%s"; cmd | getline p; close(cmd);
diff=(s-p)/86400; if(diff>2){print prev"->"d" ("diff" days)"; exit}}
prev=d
' <<<"$CSV"
)"

if [ -z "$GAP" ]; then
echo "TC-5 RESULT=PASS"; exit 0
else
echo "TC-5 RESULT=FAIL"
echo "$GAP"
exit 1
fi
