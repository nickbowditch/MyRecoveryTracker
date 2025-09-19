#!/bin/bash
PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/sleep/at2.2.txt"

adb get-state >/dev/null 2>&1 || { echo "AT-2 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "AT-2 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

for a in "$PKG".ACTION_RUN_SLEEP_ROLLUP "$PKG".ACTION_RUN_ROLLUP_SLEEP; do
  adb shell am broadcast -a "$a" -n "$PKG"/.TriggerReceiver >/dev/null 2>&1
done
sleep 2

LOG="$(adb exec-out run-as "$PKG" cat files/sleep_log.csv 2>/dev/null || printf "")"
DAILY="$(adb exec-out run-as "$PKG" cat files/daily_sleep.csv 2>/dev/null || printf "")"
[ -n "$LOG" ]   || { echo "AT-2 RESULT=FAIL (missing sleep_log.csv)" | tee "$OUT"; exit 4; }
[ -n "$DAILY" ] || { echo "AT-2 RESULT=FAIL (missing daily_sleep.csv)" | tee "$OUT"; exit 5; }

if ! printf '%s\n' "$DAILY" | awk 'NR>1{exit 1} END{exit 0}'; then :; else
  echo "AT-2 RESULT=PASS" | tee "$OUT"; exit 0
fi

LAGG="$(printf '%s\n' "$LOG" | awk -F, '
function tmin(s, a){n=split(s,a,":");return (a[1]*60 + a[2] + ((n>2)?a[3]/60:0))}
NR==1{next}
{
  ts=$1; ev=$2
  d=substr(ts,1,10); tm=substr(ts,12)
  if(ev=="SLEEP"||ev=="SLEEP_START"){have=1; sd=d; st=tmin(tm)}
  else if(ev=="WAKE"){
    if(!have){unpaired=1; next}
    ed=d; et=tmin(tm)
    if(sd==ed){add=et-st; if(add<0) add+=1440; mins[sd]+=add}
    else{add1=1440-st; if(add1<0) add1+=1440; mins[sd]+=add1; mins[ed]+=et}
    have=0
  }
}
END{
  if(have) unpaired=1
  for(k in mins) printf "%s,%d\n", k, int(mins[k]+0.5)
  printf "__UNPAIRED__=%d\n", unpaired?1:0
}')"

UNP="$(printf '%s\n' "$LAGG" | awk -F= '/^__UNPAIRED__/ {print $2}')"
[ "${UNP:-0}" -eq 0 ] || { echo "AT-2 RESULT=FAIL (unpaired events)" | tee "$OUT"; exit 6; }

LONLY="$(printf '%s\n' "$LAGG" | grep -v '^__UNPAIRED__' | sort)"
DAGG="$(printf '%s\n' "$DAILY" | awk -F, 'NR>1{printf "%s,%d\n",$1,int($4*60+0.5)}' | sort)"

if awk -F, 'FNR==NR{a[$1]=$2; s[$1]=1; next} {b[$1]=$2; s[$1]=1}
END{for(k in s){da=a[k]+0; db=b[k]+0; if( (da-db<-1)||(da-db>1) ){bad=1; break}} exit bad}' \
  <(printf '%s\n' "$LONLY") <(printf '%s\n' "$DAGG"); then
  echo "AT-2 RESULT=PASS" | tee "$OUT"; exit 0
else
  echo "AT-2 RESULT=FAIL" | tee "$OUT"; exit 1
fi
