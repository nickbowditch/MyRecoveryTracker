#!/bin/sh
PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/sleep/di3.5.txt"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "DI-3 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG"  >/dev/null 2>&1 || { echo "DI-3 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

CSV="$(adb exec-out run-as "$PKG" sh -c 'cat files/daily_sleep.csv 2>/dev/null' | tr -d '\r')"
[ -n "$CSV" ] || { echo "DI-3 RESULT=FAIL (missing csv)" | tee "$OUT"; exit 4; }

printf '%s\n' "$CSV" | awk -F',' '
function time_ok(t){ return (t=="" || t ~ /^([01][0-9]|2[0-3]):[0-5][0-9](:[0-5][0-9])?$/) }
function date_ok(d){ return (d ~ /^[0-9]{4}-[0-9]{2}-[0-9]{2}$/) }
function is_int(x){ return (x ~ /^-?[0-9]+$/) }
function is_num(x){ return (x ~ /^-?[0-9]+([.][0-9]+)?$/) }

NR==1{
  n=split($0,h,",")
  for(i=1;i<=n;i++){
    if(h[i]=="date") di=i
    if(h[i]=="sleep_time") si=i
    if(h[i]=="wake_time") wi=i
    if(h[i]=="duration_minutes") mi=i
    if(h[i]=="duration_hours") hi=i
  }
  if(!di||!si||!wi||(!mi&&!hi)){ print "DI-3 RESULT=FAIL (missing required columns)"; exit 5 }
  next
}
NR>1{
  d=$di; st=$si; wt=$wi
  vmin=(mi? $mi:"")
  vhr=(hi? $hi:"")

  r=""

  if(!date_ok(d)) r=r "DATE_FMT;"
  if(!time_ok(st) || !time_ok(wt)) r=r "TIME_FMT;"

  if(vmin!=""){
    if(!is_int(vmin)) r=r "DUR_MIN_INT;"
    else if(vmin<0 || vmin>1440) r=r "DUR_MIN_RANGE;"
    if((vmin+0)==0 && (st!="" || wt!="")) r=r "ZERO_WITH_TIMES;"
  } else if(vhr!=""){
    if(!is_num(vhr)) r=r "DUR_HR_NUM;"
    else if(vhr<0 || vhr>18) r=r "DUR_HR_RANGE;"
    if((vhr+0)==0 && (st!="" || wt!="")) r=r "ZERO_WITH_TIMES;"
  } else {
    if(st!="" || wt!="") r=r "NO_DURATION_WITH_TIMES;"
  }

  if((st=="" && wt!="") || (st!="" && wt=="")) r=r "ONE_TIME_ONLY;"

  if(r!=""){ print "BAD:" r ":" $0; bad=1 }
}
END{
  if(bad){ print "DI-3 RESULT=FAIL" } else { print "DI-3 RESULT=PASS" }
  exit(bad?1:0)
}
' | tee "$OUT"
