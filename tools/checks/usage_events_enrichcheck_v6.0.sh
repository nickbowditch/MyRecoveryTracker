#!/bin/sh
set -eu
APP="com.nick.myrecoverytracker"
RAW="files/usage_events.csv"
DAILY="files/daily_usage_events.csv"
RCV="$APP/.TriggerReceiver"
ACT="$APP.ACTION_RUN_USAGE_EVENTS_DAILY"
OUT="evidence/v6.0/usage_events/enrichcheck.txt"
mkdir -p "$(dirname "$OUT")"
fail(){ echo "ENRICH-CHK RESULT=FAIL $1" | tee "$OUT"; exit 1; }

adb get-state >/dev/null 2>&1 || fail "(no device)"
adb shell pm path "$APP" >/dev/null 2>&1 || fail "(app not installed)"

T="$(adb shell toybox date +%F | tr -d '\r')"

adb shell run-as "$APP" sh <<'IN'
set -eu
raw="files/usage_events.csv"
daily="files/daily_usage_events.csv"
today="$(toybox date +%F)"
mkdir -p files
echo "date,time,event_type,package" > "$raw"
echo "date,event_count" > "$daily"
echo "$today,12:00:00,ACTIVITY_RESUMED,com.test.x" >> "$raw"
echo "$today,12:05:00,ACTIVITY_PAUSED,com.test.x"  >> "$raw"
IN

exp=2
deadline=$(( $(date +%s) + 20 ))
cnt=0
while :; do
  adb shell cmd activity broadcast -n "$RCV" -a "$ACT" --receiver-foreground --user 0 >/dev/null 2>&1 || true
  sleep 1
  cnt="$( (adb exec-out run-as "$APP" cat "$DAILY" 2>/dev/null || true) | tr -d '\r' | awk -F, -v d="$T" 'NR>1&&$1==d{print $2;f=1;exit} END{if(!f)print 0}')"
  [ "$(date +%s)" -ge "$deadline" ] && break
  [ -n "$cnt" ] && break
done

{
  echo "EXPECTED=$exp"
  echo "OBSERVED=${cnt:-0}"
} | tee "$OUT" >/dev/null

if [ "$cnt" -eq "$exp" ]; then
  echo "ENRICH-CHK RESULT=PASS" | tee -a "$OUT"
  exit 0
else
  echo "ENRICH-CHK RESULT=FAIL (worker enriched extra events)" | tee -a "$OUT"
  exit 1
fi
