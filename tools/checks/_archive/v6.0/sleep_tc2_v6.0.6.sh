#!/bin/bash
PKG="com.nick.myrecoverytracker"
adb get-state >/dev/null 2>&1 || { echo "TC-2 RESULT=FAIL (no device)"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "TC-2 RESULT=FAIL (app not installed)"; exit 3; }

CSV="$(adb exec-out run-as "$PKG" cat files/daily_sleep.csv 2>/dev/null | tr -d $'\r')"
[ -n "$CSV" ] || { echo "TC-2 RESULT=FAIL"; exit 1; }

printf '%s\n' "$CSV" | awk -F, '
function toMin(s,  a){ n=split(s,a,":"); return (n>=2)?(a[1]*60+a[2]+((n>=3)?a[3]/60:0)):0 }
BEGIN{bad=0}
NR==1{next}
{
  st=$2; wt=$3; dh=$4;
  if (st=="" || wt=="") next;
  t1=toMin(st); t2=toMin(wt);
  diff=t2-t1; if (diff<0) diff+=1440;
  calc=diff/60.0;
  if (dh=="") next;
  obs=dh+0.0;
  if ( (calc-obs< -0.06) || (calc-obs>0.06) ) { bad=1; exit }
}
END{ exit bad?1:0 }
' || { echo "TC-2 RESULT=FAIL"; exit 1; }

echo "TC-2 RESULT=PASS"
exit 0
