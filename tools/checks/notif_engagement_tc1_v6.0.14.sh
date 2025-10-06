#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
RAW="files/notification_log.csv"
DAILY="files/daily_notification_engagement.csv"
RCV="$PKG/.TriggerReceiver"
ACT="$PKG.ACTION_RUN_NOTIFICATION_ENGAGEMENT_ROLLUP"
OUT="evidence/v6.0/notification_engagement/tc1.14.txt"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "TC1 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG"  >/dev/null 2>&1 || { echo "TC1 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

T="$(adb shell date +%F | tr -d '\r')"

adb exec-out run-as "$PKG" sh -c '
set -eu
mkdir -p files app/locks
[ -f "'"$DAILY"'" ] || printf "date,feature_schema_version,delivered,opened,open_rate\n" >"'"$DAILY"'"
if [ ! -f "'"$RAW"'" ]; then printf "ts,event,notif_id\n" >"'"$RAW"'"; fi
' >/dev/null

get_deliv(){ awk -F, -v d="$1" 'NR>1 && $1==d {v=$3; f=1} END{ if(!f) v=0; print v }'; }
get_open(){  awk -F, -v d="$1" 'NR>1 && $1==d {v=$4; f=1} END{ if(!f) v=0; print v }'; }

bd="$(adb exec-out run-as "$PKG" cat "$DAILY" | tr -d '\r' | get_deliv "$T")"
bo="$(adb exec-out run-as "$PKG" cat "$DAILY" | tr -d '\r' | get_open  "$T")"

s1="$T 12:00:05"; s2="$T 12:05:10"
id1="tc1-$RANDOM"; id2="tc1-$((RANDOM+1))"

adb exec-out run-as "$PKG" sh -c '
set -eu
f="'"$RAW"'"
printf "%s,POSTED,%s\n"  "'"$s1"'" "'"$id1"'" >>"$f"
printf "%s,CLICKED,%s\n" "'"$s1"'" "'"$id1"'" >>"$f"
printf "%s,POSTED,%s\n"  "'"$s2"'" "'"$id2"'" >>"$f"
printf "%s,CLICKED,%s\n" "'"$s2"'" "'"$id2"'" >>"$f"
' >/dev/null

deadline=$(( $(date +%s) + 25 ))
ok=1
while :; do
  adb shell cmd activity broadcast -n "$RCV" -a "$ACT" --receiver-foreground --user 0 >/dev/null 2>&1 || true
  sleep 1
  ad="$(adb exec-out run-as "$PKG" cat "$DAILY" | tr -d '\r' | get_deliv "$T")"
  ao="$(adb exec-out run-as "$PKG" cat "$DAILY" | tr -d '\r' | get_open  "$T")"
  incd=$(( ad - bd ))
  inco=$(( ao - bo ))
  if [ "$incd" -ge 2 ] && [ "$inco" -ge 2 ]; then ok=0; break; fi
  [ "$(date +%s)" -ge "$deadline" ] && break
done

[ $ok -eq 0 ] && echo "TC1 RESULT=PASS" | tee "$OUT" || echo "TC1 RESULT=FAIL (incd=$incd inco=$inco)" | tee "$OUT"
exit $ok
