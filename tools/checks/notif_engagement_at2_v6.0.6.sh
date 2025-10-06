#!/bin/sh
set -eu
APP="com.nick.myrecoverytracker"
RAW="files/notification_log.csv"
DAILY="files/daily_notification_engagement.csv"
OUT="evidence/v6.0/notification_engagement/at2.6.txt"
TOL=1

fail(){ echo "AT2 RESULT=FAIL $1" | tee "$OUT"; exit 1; }

adb get-state >/dev/null 2>&1 || fail "(no device)"
adb shell pm path "$APP" >/dev/null 2>&1 || fail "(app not installed)"

RH="$(adb exec-out run-as "$APP" sed -n '1p' "$RAW" 2>/dev/null | tr -d '\r' || true)"
DH="$(adb exec-out run-as "$APP" sed -n '1p' "$DAILY" 2>/dev/null | tr -d '\r' || true)"
[ -n "$RH" ] || fail "(missing raw)"
[ -n "$DH" ] || fail "(missing daily)"
printf '%s' "$RH" | grep -q '^ts,event,notif_id$' || fail "(bad raw header)"
printf '%s' "$DH" | grep -q '^date,feature_schema_version,delivered,opened,open_rate$' || fail "(bad daily header)"

RAW_DATES="$(adb exec-out run-as "$APP" awk -F, 'NR>1{if(length($1)>=10){print substr($1,1,10)}}' "$RAW" 2>/dev/null | sort -u || true)"
DAILY_DATES="$(adb exec-out run-as "$APP" awk -F, 'NR>1{print $1}' "$DAILY" 2>/dev/null | grep -E '^[0-9]{4}-[0-9]{2}-[0-9]{2}$' | sort -u || true)"

TMP1="$(mktemp)"; TMP2="$(mktemp)"; trap 'rm -f "$TMP1" "$TMP2"' EXIT
printf '%s\n' "$RAW_DATES" > "$TMP1"
printf '%s\n' "$DAILY_DATES" > "$TMP2"
INTER="$(grep -Fxf "$TMP1" "$TMP2" | sort -u || true)"
[ -n "$INTER" ] || fail "(no overlapping date between raw and daily)"

RESULTS=""
BAD=0
while IFS= read -r D; do
  [ -n "$D" ] || continue
  DD="$(adb exec-out run-as "$APP" awk -F, -v d="$D" 'NR>1 && $1==d{print $3","$4; exit}' "$DAILY" 2>/dev/null | tr -d '\r' || true)"
  [ -n "$DD" ] || { RESULTS="${RESULTS}MISS $D (no daily row)\n"; BAD=1; continue; }
  DD_DEL="$(printf '%s' "$DD" | cut -d, -f1)"
  DD_OPN="$(printf '%s' "$DD" | cut -d, -f2)"
  RD="$(adb exec-out run-as "$APP" awk -F, -v d="$D" '
  NR==1{next}
  {
    ts=$1; ev=toupper($2); id=$3
    if(length(ts)<10||id=="") next
    day=substr(ts,1,10)
    if(day!=d) next
    if(ev=="POSTED"||ev=="DELIVERED"){p[id]=1}
    else if(ev=="CLICKED"||ev=="CLICK"||ev=="OPENED"){c[id]=1}
  }
  END{pd=0; for(k in p) pd++; cd=0; for(k in c) cd++; print pd "," cd}' "$RAW" 2>/dev/null | tr -d '\r' || true)"
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
