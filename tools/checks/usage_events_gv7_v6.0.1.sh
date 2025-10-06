#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
RAW="files/usage_events.csv"
DAILY="files/daily_usage_events.csv"
LOCK="app/locks/daily_usage_events.header"
RCV="$PKG/.TriggerReceiver"
ACT="$PKG.ACTION_RUN_USAGE_EVENTS_DAILY"
OUT="evidence/v6.0/usage_events_daily/gv7.1.txt"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "GV7 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "GV7 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

T="$(adb shell date +%F | tr -d '\r')"

adb shell run-as "$PKG" sh <<IN
set -eu
raw="$RAW"
daily="$DAILY"
lock="$LOCK"
hdr="date,event_count"
mkdir -p app/locks files
echo "\$hdr" > "\$lock"
echo "\$hdr" > "\$daily"
echo "date,time,event_type,package" > "\$raw"
echo "$T,10:00:00,ACTIVITY_RESUMED,com.test.gv7a" >> "\$raw"
echo "$T,10:05:00,ACTIVITY_PAUSED,com.test.gv7a"  >> "\$raw"
echo "$T,11:00:00,ACTIVITY_RESUMED,com.test.gv7b" >> "\$raw"
echo "$T,11:10:00,ACTIVITY_PAUSED,com.test.gv7b"  >> "\$raw"
IN

adb shell cmd activity broadcast -n "$RCV" -a "$ACT" --receiver-foreground --user 0 >/dev/null 2>&1 || true
sleep 2

hdr="$(adb exec-out run-as "$PKG" head -n1 "$DAILY" | tr -d '\r')"
row="$(adb exec-out run-as "$PKG" awk -F, -v d="$T" 'NR>1&&$1==d{print;exit}' "$DAILY" | tr -d '\r')"

[ "$hdr" = "date,event_count" ] || { echo "GV7 RESULT=FAIL (bad header: $hdr)" | tee "$OUT"; exit 1; }
[ -n "$row" ] || { echo "GV7 RESULT=FAIL (missing row for $T)" | tee "$OUT"; exit 1; }

cnt="$(echo "$row" | awk -F, '{print $2}')"
[ "$cnt" -ge 0 ] 2>/dev/null || { echo "GV7 RESULT=FAIL (bad count=$cnt)" | tee "$OUT"; exit 1; }

echo "GV7 RESULT=PASS" | tee "$OUT"
exit 0
