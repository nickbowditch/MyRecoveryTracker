#!/bin/bash
PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/sleep/di3.1.txt"

adb get-state >/dev/null 2>&1 || { echo "DI-3 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG"  >/dev/null 2>&1 || { echo "DI-3 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

CSV="$(adb exec-out run-as "$PKG" sh -c 'cat files/daily_sleep.csv 2>/dev/null' | tr -d $'\r')"
[ -n "$CSV" ] || { echo "DI-3 RESULT=FAIL (missing csv)" | tee "$OUT"; exit 4; }

HEAD="$(printf '%s\n' "$CSV" | head -n1)"

dur_col="$(awk -F, -v h="$HEAD" 'BEGIN{
n=split(h,a,",");
for(i=1;i<=n;i++){
if(a[i]=="duration_minutes"){ print i; exit }
if(a[i]=="duration_hours"){ print -i; exit }
}
print 0
}')"

bad=""
if [ "$dur_col" -eq 0 ]; then
bad="(no duration column)"
else
if [ "$dur_col" -gt 0 ]; then
BAD_ROWS="$(awk -F, -v c="$dur_col" '
NR>1{
v=$c
if(v !~ /^-?[0-9]+$/) {print $0; next}
if(v<0 || v>1440)     {print $0; next}
if(v==0 && ($2!="" || $3!="")) {print $0; next}
}' <<<"$CSV")"
else
idx=$(( -dur_col ))
BAD_ROWS="$(awk -F, -v c="$idx" '
function isnum(x){ return (x ~ /^-?[0-9]+(\.[0-9]+)?$/) }
NR>1{
v=$c
if(v!="" && !isnum(v)) {print $0; next}
if(v!="" && (v<0 || v>18)) {print $0; next}
if(v==0 && ($2!="" || $3!="")) {print $0; next}
if($2!="" && $2 !~ /^[0-2][0-9]:[0-5][0-9](:[0-5][0-9])?$/)/^[0-2][0-9]:[0-5]0-9?$/){print $0; next}
if($3!="" && $3 !~ /^[0-2][0-9]:[0-5][0-9](:[0-5][0-9])?$/)/^[0-2][0-9]:[0-5]0-9?$/){print $0; next}
}' <<<"$CSV")"
fi
bad="$BAD_ROWS"
fi

if [ -z "$bad" ]; then
echo "DI-3 RESULT=PASS" | tee "$OUT"
exit 0
else
{
echo "DI-3 RESULT=FAIL"
printf "%s\n" "$bad"
} | tee "$OUT"
exit 1
fi
