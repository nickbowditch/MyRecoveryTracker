#!/bin/sh
set -eu
APP="com.nick.myrecoverytracker"
RAW="files/usage_events.csv"
DAILY="files/daily_usage_events.csv"
RCV="$APP/.TriggerReceiver"
ACT="$APP.ACTION_RUN_USAGE_EVENTS_DAILY"
OUT="evidence/v6.0/usage_events/at2_rerun.txt"
mkdir -p "$(dirname "$OUT")"
fail(){ echo "AT2-RERUN RESULT=FAIL $1" | tee "$OUT"; exit 1; }

adb get-state >/dev/null 2>&1 || fail "(no device)"
adb shell pm path "$APP" >/dev/null 2>&1 || fail "(app not installed)"

RH="$(adb exec-out run-as "$APP" sed -n '1p' "$RAW" 2>/dev/null | tr -d '\r' || true)"
DH="$(adb exec-out run-as "$APP" sed -n '1p' "$DAILY" 2>/dev/null | tr -d '\r' || true)"
[ "$RH" = "date,time,event_type,package" ] || fail "(raw header mismatch)"
[ "$DH" = "date,event_count" ] || fail "(daily header mismatch)"

T="$(adb shell toybox date +%F | tr -d '\r')"

adb shell run-as "$APP" sh <<IN
set -eu
raw="$RAW"
daily="$DAILY"
mkdir -p files
echo "date,time,event_type,package" > "\$raw"
echo "date,event_count" > "\$daily"
echo "\$T,12:00:00,ACTIVITY_RESUMED,com.test.a" >> "\$raw"
echo "\$T,12:05:00,ACTIVITY_PAUSED,com.test.a"  >> "\$raw"
echo "\$T,13:00:00,ACTIVITY_RESUMED,com.test.b" >> "\$raw"
echo "\$T,13:05:00,ACTIVITY_PAUSED,com.test.b"  >> "\$raw"
IN

exp=4
deadline=$(( $(date +%s) + 30 ))
cnt=0
while :; do
  adb shell cmd activity broadcast -n "$RCV" -a "$ACT" --receiver-foreground --user 0 >/dev/null 2>&1 || true
  sleep 1
  cnt="$( (adb exec-out run-as "$APP" cat "$DAILY" 2>/dev/null || true) | tr -d '\r' | awk -F, -v d="$T" 'NR>1&&$1==d{print $2;exit}' )"
  [ -n "${cnt:-}" ] || cnt=0
  [ "$(date +%s)" -ge "$deadline" ] && break
  [ "$cnt" -ge 1 ] && break
done

RAW_N="$(adb exec-out run-as "$APP" awk -F, -v d="$T" 'NR>1&&$1==d{n++} END{print n+0}' "$RAW" 2>/dev/null || echo 0)"

{
  echo "EXPECTED_RAW=$exp"
  echo "RAW_COUNT=$RAW_N"
  echo "DAILY_COUNT=$cnt"
} | tee "$OUT" >/dev/null

if [ "$RAW_N" -eq "$cnt" ] && [ "$cnt" -eq "$exp" ]; then
  echo "AT2-RERUN RESULT=PASS" | tee -a "$OUT"
  exit 0
else
  echo "AT2-RERUN RESULT=FAIL" | tee -a "$OUT"
  exit 1
fi
