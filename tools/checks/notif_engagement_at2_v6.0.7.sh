#!/bin/sh
set -eu
APP="com.nick.myrecoverytracker"
RAW="files/notification_log.csv"
DAILY="files/daily_notification_engagement.csv"
OUT="evidence/v6.0/notification_engagement/at2.7.txt"
TOL=1

fail(){ echo "AT2 RESULT=FAIL $1" | tee "$OUT"; exit 1; }

adb get-state >/dev/null 2>&1 || fail "(no device)"
adb shell pm path "$APP" >/dev/null 2>&1 || fail "(app not installed)"

RH="$(adb exec-out run-as "$APP" sed -n '1p' "$RAW" 2>/dev/null | tr -d '\r' || true)"
DH="$(adb exec-out run-as "$APP" sed -n '1p' "$DAILY" 2>/dev/null | tr -d '\r' || true)"
[ -n "$RH" ] || fail "(missing raw)"
[ -n "$DH" ] || fail "(missing daily)"
[ "$RH" = "ts,event,notif_id" ] || fail "(bad raw header)"
[ "$DH" = "date,feature_schema_version,delivered,opened,open_rate" ] || fail "(bad daily header)"

RAW_DATES="$(adb exec-out run-as "$APP" awk -F, 'NR>1{if(length($1)>=10){print substr($1,1,10)}}' "$RAW" 2>/dev/null | sort -u || true)"
DAILY_DATES="$(adb exec-out run-as "$APP" awk -F, 'NR>1{print $1}' "$DAILY" 2>/dev/null | grep -E '^[0-9]{4}-[0-9]{2}-[0-9]{2}$' | sort -u || true)"

TMP1="$(mktemp)"; TMP2="$(mktemp)"; trap 'rm -f "$TMP1" "$TMP2"' EXIT
printf '%s\n' "$RAW_DATES" > "$TMP1"
printf '%s\n' "$DAILY_DATES" > "$TMP2"
INTER="$(grep -Fxf "$TMP1" "$TMP2" | sort -u || true)"
[ -n "$INTER" ] || fail "(no overlapping date between raw and daily)"

pair_counts_for_day() {
  D="$1"
  adb exec-out run-as "$APP" awk -F, -v D="$D" '
  function to_epoch(s,   cmd,sec){ cmd="toybox date -d \"" s "\" +%s"; cmd | getline sec; close(cmd); return sec+0 }
  NR==1{next}
  {
    ts=$1; ev=toupper($2); id=$3
    if(ts==""||id=="") next
    ep=to_epoch(ts)
    day=substr(ts,1,10)
    if(ev=="POSTED"||ev=="DELIVERED"){
      pcount[id]++; p_ts=id SUBSEP pcount[id]; posted[p_ts]=ep; p_day[p_ts]=day
      if(day==D) delivered_on_D++
    } else if(ev=="CLICKED"||ev=="CLICK"||ev=="OPENED"){
      ccount[id]++; c_ts[id,ccount[id]]=ep
    }
  }
  END{
    opened_on_D=0
    for(k in posted){
      split(k, parts, SUBSEP); id=parts[1]; idx=parts[2]+0
      if(p_day[k]!=D) continue
      ptime=posted[k]+0
      # find first click >= ptime
      found=0
      for(i=1; i<=ccount[id]; i++){
        ct=c_ts[id,i]+0
        if(ct>=ptime){ found=1; cts=ct; break }
      }
      if(found){ opened_on_D++ }
    }
    printf "%d,%d\n", delivered_on_D+0, opened_on_D+0
  }' "$RAW" 2>/dev/null | tr -d '\r'
}

RESULTS=""
BAD=0
while IFS= read -r D; do
  [ -n "$D" ] || continue
  DD="$(adb exec-out run-as "$APP" awk -F, -v d="$D" 'NR>1 && $1==d{print $3","$4; exit}' "$DAILY" 2>/dev/null | tr -d '\r' || true)"
  [ -n "$DD" ] || { RESULTS="${RESULTS}MISS $D (no daily row)\n"; BAD=1; continue; }
  DD_DEL="$(printf '%s' "$DD" | cut -d, -f1)"
  DD_OPN="$(printf '%s' "$DD" | cut -d, -f2)"
  RD="$(pair_counts_for_day "$D")"
  [ -n "$RD" ] || RD="0,0"
  RD_DEL="$(printf '%s' "$RD" | cut -d, -f1)"
  RD_OPN="$(printf '%s' "$RD" | cut -d, -f2)"
  diff_abs(){ a=$1; b=$2; d=$((a-b)); [ $d -lt 0 ] && d=$((-d)); echo $d; }
  DDEL="$(diff_abs "$RD_DEL" "$DD_DEL")"
  DOPN="$(diff_abs "$RD_OPN" "$DD_OPN")"
  if [ "$DDEL" -le "$TOL" ] && [ "$DOPN" -le "$TOL" ]; then
    RESULTS="${RESULTS}OK  $D raw=${RD_DEL},${RD_OPN} daily=${DD_DEL},${DD_OPN}\n"
  else
    RESULTS="${RESULTS}BAD $D raw=${RD_DEL},${RD_OPN} daily=${DD_DEL},${DD_OPN}\n"
    BAD=1
  fi
done <<EOF_D
$INTER
EOF_D

printf "%b" "$RESULTS" | tee "$OUT"
[ $BAD -eq 0 ] && echo "AT2 RESULT=PASS" | tee -a "$OUT" || { echo "AT2 RESULT=FAIL" | tee -a "$OUT"; exit 1; }
