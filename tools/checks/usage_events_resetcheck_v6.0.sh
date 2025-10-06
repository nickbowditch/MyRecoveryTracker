#!/bin/sh
set -eu
APP="com.nick.myrecoverytracker"
RAW="files/usage_events.csv"
DAILY="files/daily_usage_events.csv"
RCV="$APP/.TriggerReceiver"
A1="$APP.ACTION_RUN_USAGE_EVENTS_DAILY"
A2="$APP.ACTION_RUN_USAGE_EVENTS_ROLLUP"
OUT="evidence/v6.0/usage_events/resetcheck.txt"
mkdir -p "$(dirname "$OUT")"
fail(){ echo "RESET-CHK RESULT=FAIL $1" | tee "$OUT"; exit 1; }
adb get-state >/dev/null 2>&1 || fail "(no device)"
adb shell pm path "$APP" >/dev/null 2>&1 || fail "(app not installed)"
Y="$(adb shell toybox date -d "@$(( $(adb shell toybox date +%s)-86400 ))" +%F | tr -d '\r')"
T="$(adb shell toybox date +%F | tr -d '\r')"
adb shell run-as "$APP" sh <<IN
set -eu
raw="$RAW"
daily="$DAILY"
mkdir -p files
echo "date,event_count" > "\$daily"
echo "date,time,event_type,package" > "\$raw"
echo "$Y,09:00:00,ACTIVITY_RESUMED,com.test.y"  >> "\$raw"
echo "$Y,09:10:00,ACTIVITY_PAUSED,com.test.y"   >> "\$raw"
echo "$T,12:00:00,ACTIVITY_RESUMED,com.test.t1" >> "\$raw"
echo "$T,12:05:00,ACTIVITY_PAUSED,com.test.t1"  >> "\$raw"
echo "$T,13:00:00,ACTIVITY_RESUMED,com.test.t2" >> "\$raw"
IN
exp_y=2
exp_t=3
deadline=$(( $(date +%s) + 30 ))
y_cnt=0
t_cnt=0
while :; do
  adb shell cmd activity broadcast -n "$RCV" -a "$A1" --receiver-foreground --user 0 >/dev/null 2>&1 || true
  adb shell cmd activity broadcast -n "$RCV" -a "$A2" --receiver-foreground --user 0 >/dev/null 2>&1 || true
  sleep 1
  y_cnt="$( (adb exec-out run-as "$APP" cat "$DAILY" 2>/dev/null || true) | tr -d '\r' | awk -F, -v d="$Y" 'NR>1&&$1==d{print $2;f=1;exit} END{if(!f)print 0}')"
  t_cnt="$( (adb exec-out run-as "$APP" cat "$DAILY" 2>/dev/null || true) | tr -d '\r' | awk -F, -v d="$T" 'NR>1&&$1==d{print $2;f=1;exit} END{if(!f)print 0}')"
  [ "$(date +%s)" -ge "$deadline" ] && break
  [ -n "$y_cnt" ] && [ -n "$t_cnt" ] && break
done
{
  echo "EXPECTED_Y=$exp_y EXPECTED_T=$exp_t"
  echo "OBS_Y=${y_cnt:-0} OBS_T=${t_cnt:-0}"
} | tee "$OUT" >/dev/null
if [ "$y_cnt" -eq "$exp_y" ] && [ "$t_cnt" -eq "$exp_t" ]; then
  echo "RESET-CHK RESULT=PASS" | tee -a "$OUT"
  exit 0
else
  echo "RESET-CHK RESULT=FAIL" | tee -a "$OUT"
  exit 1
fi
