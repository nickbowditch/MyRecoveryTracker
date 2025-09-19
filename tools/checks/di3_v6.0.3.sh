#!/bin/bash
PKG="com.nick.myrecoverytracker"
adb get-state >/dev/null 2>&1 || { echo "DI-3 RESULT=FAIL (no device)"; exit 2; }
adb shell pm path "$PKG"  >/dev/null 2>&1 || { echo "DI-3 RESULT=FAIL (app not installed)"; exit 3; }

CSV="$(adb exec-out run-as "$PKG" sh -c 'cat files/daily_unlocks.csv 2>/dev/null' | tr -d $'\r')"
[ -n "$CSV" ] || { echo "DI-3 RESULT=FAIL (missing csv)"; exit 4; }

HEAD="$(printf '%s\n' "$CSV" | head -n1)"
CCOL="$(awk -F, -v h="$HEAD" 'BEGIN{n=split(h,a,","); for(i=1;i<=n;i++) if(a[i]=="daily_unlocks"){print i; exit}; print 2}')"

BAD="$(awk -F, -v c="$CCOL" 'NR>1{
v=$c
if(v !~ /^[0-9]+$/) {print $0; next}
if(v<0 || v>1440)   {print $0; next}
}' <<<"$CSV")"

if [ -z "$BAD" ]; then
echo "DI-3 RESULT=PASS"
exit 0
else
echo "DI-3 RESULT=FAIL"
printf "%s\n" "$BAD"
exit 1
fi
