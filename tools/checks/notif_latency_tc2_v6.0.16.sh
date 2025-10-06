#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
RAW="files/notification_log.csv"
DAILY="files/daily_notification_latency.csv"
RCV="$PKG/.TriggerReceiver"
ACT="$PKG.ACTION_RUN_NOTIFICATION_LATENCY_ROLLUP"
OUT="evidence/v6.0/notification_latency/tc2.16.txt"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "TC2 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "TC2 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

Y="$(adb shell toybox date -d "@$(( $(adb shell toybox date +%s)-86400 ))" +%F | tr -d '\r')"
T="$(adb shell date +%F | tr -d '\r')"

adb exec-out run-as "$PKG" sh -c '
set -eu
y="$(toybox date -d "@$(( $(toybox date +%s)-86400 ))" +%F)"
t="$(toybox date +%F)"
echo "$y 23:59:00,POSTED,lat-tc2"  > "'"$RAW"'"
echo "$t 00:01:00,CLICKED,lat-tc2" >> "'"$RAW"'"
' >/dev/null

getcount(){ awk -F, -v d="$1" 'NR>1&&$1==d{print $NF;f=1;exit} END{if(!f)print 0}'; }
before_y="$(adb exec-out run-as "$PKG" cat "$DAILY" 2>/dev/null | tr -d '\r' | getcount "$Y")"
before_t="$(adb exec-out run-as "$PKG" cat "$DAILY" 2>/dev/null | tr -d '\r' | getcount "$T")"

deadline=$(( $(date +%s) + 25 ))
ok=1
while :; do
  adb shell cmd activity broadcast -n "$RCV" -a "$ACT" --receiver-foreground --user 0 >/dev/null 2>&1 || true
  sleep 1
  after_y="$(adb exec-out run-as "$PKG" cat "$DAILY" 2>/dev/null | tr -d '\r' | getcount "$Y")"
  after_t="$(adb exec-out run-as "$PKG" cat "$DAILY" 2>/dev/null | tr -d '\r' | getcount "$T")"
  inc_y=$(( ${after_y:-0} - ${before_y:-0} ))
  inc_t=$(( ${after_t:-0} - ${before_t:-0} ))
  if [ "$inc_y" -ge 1 ] && [ "${inc_t:-0}" -eq 0 ]; then ok=0; break; fi
  [ "$(date +%s)" -ge "$deadline" ] && break
done

[ $ok -eq 0 ] && echo "TC2 RESULT=PASS" | tee "$OUT" || echo "TC2 RESULT=FAIL (inc_y=${inc_y:-0} inc_t=${inc_t:-0})" | tee "$OUT"
exit $ok
