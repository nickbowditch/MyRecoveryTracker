#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
RAW="files/notification_log.csv"
DAILY="files/daily_notification_latency.csv"
RCV="$PKG/.TriggerReceiver"
ACT="$PKG.ACTION_RUN_NOTIFICATION_LATENCY_ROLLUP"
OUT="evidence/v6.0/notification_latency/tc2.20.txt"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "TC2 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "TC2 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

Y="$(adb shell toybox date -d "@$(( $(adb shell toybox date +%s)-86400 ))" +%F | tr -d '\r')"
T="$(adb shell toybox date +%F | tr -d '\r')"

adb shell run-as "$PKG" sh <<IN
set -eu
raw="$RAW"
daily="$DAILY"
mkdir -p files
# reset daily to known header
printf "date,feature_schema_version,p50_ms,p90_ms,p99_ms,count\n" >"\$daily"
# seed:
#  - same-day pair on Y (counts to Y)
#  - cross-midnight pair Y->T (ignored by worker)
echo "ts,event,notif_id" > "\$raw"
echo "$Y 23:00:00,POSTED,tc2-sameday"  >> "\$raw"
echo "$Y 23:05:00,CLICKED,tc2-sameday" >> "\$raw"
echo "$Y 23:59:00,POSTED,tc2-xmid"     >> "\$raw"
echo "$T 00:01:00,CLICKED,tc2-xmid"    >> "\$raw"
IN

getcount(){ awk -F, -v d="$1" 'NR>1&&$1==d{print $NF;f=1;exit} END{if(!f)print 0}'; }

deadline=$(( $(date +%s) + 30 ))
pass=1
while :; do
  adb shell cmd activity broadcast -n "$RCV" -a "$ACT" --receiver-foreground --user 0 >/dev/null 2>&1 || true
  sleep 1
  y_cnt="$( (adb exec-out run-as "$PKG" cat "$DAILY" 2>/dev/null || true) | tr -d '\r' | getcount "$Y")"
  t_cnt="$( (adb exec-out run-as "$PKG" cat "$DAILY" 2>/dev/null || true) | tr -d '\r' | getcount "$T")"
  if [ "${y_cnt:-0}" -ge 1 ] && [ "${t_cnt:-0}" -eq 0 ]; then pass=0; break; fi
  [ "$(date +%s)" -ge "$deadline" ] && break
done

[ $pass -eq 0 ] && echo "TC2 RESULT=PASS" | tee "$OUT" || echo "TC2 RESULT=FAIL (y_cnt=${y_cnt:-0} t_cnt=${t_cnt:-0})" | tee "$OUT"
exit $pass
