#!/bin/bash
PKG="com.nick.myrecoverytracker"
adb get-state >/dev/null 2>&1 || { echo "AT-2 RESULT=FAIL (no device)"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "AT-2 RESULT=FAIL (app not installed)"; exit 3; }

RAW="$(adb exec-out run-as "$PKG" cat files/unlock_log.csv 2>/dev/null || printf "")"
DAILY="$(adb exec-out run-as "$PKG" cat files/daily_unlocks.csv 2>/dev/null || printf "")"
[ -n "$RAW" ] && [ -n "$DAILY" ] || { echo "AT-2 RESULT=FAIL (missing csv)"; exit 4; }

awk -F, '
BEGIN{ bad=0 }
FNR==1 && NR==1 { next }               # skip header of RAW if present
NR==FNR && FNR>1 {                     # first pass: unlock_log.csv
d=substr($1,1,10)
if (d ~ /^[0-9]{4}-[0-9]{2}-[0-9]{2}$/) rc[d]++
next
}
FNR==1 { next }                        # skip header of DAILY
FNR>1 {
d=$1
c = ($2 ~ /^v[0-9.]+$/ && NF>=3) ? $3 : $2
dc[d]=c
}
END{
for (d in rc) {
if (dc[d]"" != rc[d]"") { print d"," rc[d]"," dc[d]; bad=1 }
}
for (d in dc) {
if (!(d in rc) && dc[d] != 0) { print d",0," dc[d]; bad=1 }
}
if (bad==0) { print "AT-2 RESULT=PASS" } else { print "AT-2 RESULT=FAIL" }
}
' <(printf "%s\n" "$RAW") <(printf "%s\n" "$DAILY") | {
read line
if [ "$line" = "AT-2 RESULT=PASS" ]; then
echo "$line"; exit 0
else
echo "$line"
sed -n '2,$p'
exit 1
fi
}
