#!/bin/bash
PKG="com.nick.myrecoverytracker"
adb get-state >/dev/null 2>&1 || { echo "TC-4 RESULT=FAIL (no device)"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "TC-4 RESULT=FAIL (app not installed)"; exit 3; }

CSV="$(adb exec-out run-as "$PKG" cat files/daily_sleep.csv 2>/dev/null | tr -d $'\r')"
[ -n "$CSV" ] || { echo "TC-4 RESULT=FAIL"; exit 1; }

printf '%s\n' "$CSV" | awk -F, '
function okTime(s){ return (s=="" || s ~ /^[0-2][0-9]:[0-5][0-9](:[0-5][0-9])?$/) }
BEGIN{bad=0}
NR==1{next}
{
  st=$2; wt=$3; dh=$4;
  if (!okTime(st) || !okTime(wt)) { bad=1; exit }
  if (dh!="" && dh !~ /^-?[0-9]+(\.[0-9]+)?$/) { bad=1; exit }
  if (dh!="") {
    v=dh+0.0;
    if (v<0 || v>18) { bad=1; exit }
    if (v==0 && (st!="" || wt!="")) { bad=1; exit }
  }
}
END{ exit bad?1:0 }
' || { echo "TC-4 RESULT=FAIL"; exit 1; }

echo "TC-4 RESULT=PASS"
exit 0
