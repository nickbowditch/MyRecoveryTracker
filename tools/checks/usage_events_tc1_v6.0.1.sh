#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
RAW="files/usage_events.csv"
DAILY="files/daily_usage_events.csv"
RCV="$PKG/.TriggerReceiver"
ACT="$PKG.ACTION_RUN_USAGE_EVENTS_DAILY"
OUT="evidence/v6.0/usage_events/tc1.1.txt"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "TC1 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "TC1 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

T="$(adb shell toybox date +%F | tr -d '\r')"
Y="$(adb shell toybox date -d "@$(( $(adb shell toybox date +%s)-86400 ))" +%F | tr -d '\r')"
TM="$(adb shell toybox date -d "@$(( $(adb shell toybox date +%s)+86400 ))" +%F | tr -d '\r')"

adb shell run-as "$PKG" sh <<'IN'
set -eu
raw="files/usage_events.csv"
daily="files/daily_usage_events.csv"
mkdir -p files
[ -f "$daily" ] || printf "date,event_count\n" >"$daily"
t="$(toybox date +%F)"
echo "date,time,event_type,package" > "$raw"
echo "$t,12:00:00,ACTIVITY_RESUMED,com.test.app1" >> "$raw"
echo "$t,12:05:00,ACTIVITY_PAUSED,com.test.app1"  >> "$raw"
echo "$t,12:10:00,ACTIVITY_RESUMED,com.test.app2" >> "$raw"
echo "$t,12:15:00,ACTIVITY_PAUSED,com.test.app2"  >> "$raw"
IN

deadline=$(( $(date +%s) + 25 ))
pass=1
nT=0; nY=0; nTM=0
while :; do
  adb shell cmd activity broadcast -n "$RCV" -a "$ACT" --receiver-foreground --user 0 >/dev/null 2>&1 || true
  sleep 1
  nT="$(adb exec-out run-as "$PKG" awk -F, -v d="$T"  'NR>1&&$1==d{c++} END{print c+0}' "$DAILY" 2>/dev/null | tr -d '\r' || echo 0)"
  [ "$nT" -eq 1 ] && { pass=0; break; }
  nY="$(adb exec-out run-as "$PKG" awk -F, -v d="$Y"  'NR>1&&$1==d{c++} END{print c+0}' "$DAILY" 2>/dev/null | tr -d '\r' || echo 0)"
  nTM="$(adb exec-out run-as "$PKG" awk -F, -v d="$TM" 'NR>1&&$1==d{c++} END{print c+0}' "$DAILY" 2>/dev/null | tr -d '\r' || echo 0)"
  if [ "$nT" -eq 0 ] && { [ "$nY" -eq 1 ] || [ "$nTM" -eq 1 ]; } && [ $((nY+nTM)) -eq 1 ]; then
    pass=0; break
  fi
  [ "$(date +%s)" -ge "$deadline" ] && break
done

[ $pass -eq 0 ] && echo "TC1 RESULT=PASS" | tee "$OUT" || echo "TC1 RESULT=FAIL (rows_T=$nT rows_Y=$nY rows_TM=$nTM)" | tee "$OUT"
exit $pass
