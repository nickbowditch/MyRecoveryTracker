#!/bin/sh
set -eu
APP="com.nick.myrecoverytracker"
RAW="files/usage_events.csv"
DAILY="files/daily_usage_events.csv"
RCV="$APP/.TriggerReceiver"
A1="$APP.ACTION_RUN_USAGE_EVENTS_DAILY"
A2="$APP.ACTION_RUN_USAGE_EVENTS_ROLLUP"
OUT="evidence/v6.0/usage_events/driftcheck.txt"
mkdir -p "$(dirname "$OUT")"
fail(){ echo "DRIFT-CHK RESULT=FAIL $1" | tee "$OUT"; exit 1; }

adb get-state >/dev/null 2>&1 || fail "(no device)"
adb shell pm path "$APP" >/dev/null 2>&1 || fail "(app not installed)"

Y="$(adb shell toybox date -d "@$(( $(adb shell toybox date +%s)-86400 ))" +%F | tr -d '\r')"
T="$(adb shell toybox date +%F | tr -d '\r')"
Ym1="$(adb shell toybox date -d "@$(( $(adb shell toybox date +%s)-172800 ))" +%F | tr -d '\r')"
Tp1="$(adb shell toybox date -d "@$(( $(adb shell toybox date +%s)+86400 ))" +%F | tr -d '\r')"

adb shell run-as "$APP" sh <<IN
set -eu
raw="$RAW"
daily="$DAILY"
mkdir -p files
echo "date,event_count" > "\$daily"
echo "date,time,event_type,package" > "\$raw"
echo "$Y,23:59:00,ACTIVITY_RESUMED,com.example.y1" >> "\$raw"
echo "$Y,23:59:30,ACTIVITY_PAUSED,com.example.y1"  >> "\$raw"
echo "$Y,12:00:00,USER_INTERACTION,com.example.y2" >> "\$raw"
echo "$T,00:01:00,ACTIVITY_RESUMED,com.example.t1" >> "\$raw"
echo "$T,12:05:00,ACTIVITY_PAUSED,com.example.t1"  >> "\$raw"
echo "$T,13:00:00,ACTIVITY_RESUMED,com.example.t2" >> "\$raw"
echo "$T,14:00:00,SHORTCUT_INVOCATION,com.example.t3" >> "\$raw"
IN

exp_y=3
exp_t=4

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

other="$(adb exec-out run-as "$APP" awk -F, -v y="$Y" -v t="$T" 'NR>1 && $1!=y && $1!=t && $2+0>0 {print $1":"$2}' "$DAILY" 2>/dev/null | tr -d '\r' || true)"

{
  echo "EXPECTED_Y=$exp_y EXPECTED_T=$exp_t"
  echo "OBS_Y=${y_cnt:-0} OBS_T=${t_cnt:-0}"
  echo "OTHER_NONZERO_DATES:"
  if [ -n "$other" ]; then printf '%s\n' "$other"; else echo "[none]"; fi
} | tee "$OUT" >/dev/null

if [ "${y_cnt:-0}" -eq "$exp_y" ] && [ "${t_cnt:-0}" -eq "$exp_t" ] && [ -z "$other" ]; then
  echo "DRIFT-CHK RESULT=PASS" | tee -a "$OUT"
  exit 0
else
  echo "DRIFT-CHK RESULT=FAIL" | tee -a "$OUT"
  exit 1
fi
