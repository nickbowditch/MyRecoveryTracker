#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/sleep/at2.3.txt"
ACT1="$PKG.ACTION_RUN_SLEEP_ROLLUP"
ACT2="$PKG.ACTION_RUN_ROLLUP_SLEEP"
RCV="$PKG/.TriggerReceiver"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "AT-2 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "AT-2 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

adb shell am broadcast -a "$ACT1" -n "$RCV" >/dev/null 2>&1 || true
adb shell am broadcast -a "$ACT2" -n "$RCV" >/dev/null 2>&1 || true
sleep 2

LOG="$(adb exec-out run-as "$PKG" cat files/sleep_log.csv 2>/dev/null | tr -d '\r' || true)"
DUR="$(adb exec-out run-as "$PKG" cat files/daily_sleep_duration.csv 2>/dev/null | tr -d '\r' || true)"

[ -n "$LOG" ] || { echo "AT-2 RESULT=FAIL (missing sleep_log.csv)" | tee "$OUT"; exit 4; }
[ -n "$DUR" ] || { echo "AT-2 RESULT=FAIL (missing daily_sleep_duration.csv)" | tee "$OUT"; exit 5; }

if printf '%s\n' "$DUR" | awk -F, 'NR>1{exit 1} END{exit 0}'; then
  echo "AT-2 RESULT=PASS (no rows yet)" | tee "$OUT"
  exit 0
fi

LAGG="$(printf '%s\n' "$LOG" | awk -F, '
function tmin(s,a){n=split(s,a,":");return(a[1]*60+a[2]+((n>2)?a[3]/60:0))}
NR==1{next}
{ts=$1;ev=$2;d=substr(ts,1,10);tm=substr(ts,12)
 if(ev=="SLEEP"||ev=="SLEEP_START"){have=1;sd=d;st=tmin(tm)}
 else if(ev=="WAKE"){if(!have)next;ed=d;et=tmin(tm)
  if(sd==ed){add=et-st;if(add<0)add+=1440;mins[sd]+=add}
  else{add1=1440-st;if(add1<0)add1+=1440;mins[sd]+=add1;mins[ed]+=et}
  have=0}}
END{for(k in mins)printf"%s,%d\n",k,int(mins[k]+0.5)}')"

DAGG="$(printf '%s\n' "$DUR" | awk -F, 'NR>1{printf "%s,%d\n",$1,int($2*60+0.5)}' | sort)"

if awk -F, 'FNR==NR{a[$1]=$2;next}{b[$1]=$2}
END{bad=0;for(k in a){if((a[k]-b[k]<-1)||(a[k]-b[k]>1)){bad=1}}exit bad}' \
<(printf '%s\n' "$LAGG" | sort) <(printf '%s\n' "$DAGG"); then
  echo "AT-2 RESULT=PASS" | tee "$OUT"; exit 0
else
  echo "AT-2 RESULT=FAIL" | tee "$OUT"; exit 1
fi
