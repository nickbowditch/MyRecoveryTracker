#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
RAW="files/notification_log.csv"
DAILY="files/daily_notification_engagement.csv"
LOCK="app/locks/daily_notif_engagement.header"
RCV="$PKG/.TriggerReceiver"
ACT="$PKG.ACTION_RUN_NOTIFICATION_ENGAGEMENT_ROLLUP"
EXP="date,feature_schema_version,delivered,opened,open_rate"
OUT="evidence/v6.0/notification_engagement/gv7.21.txt"
mkdir -p "$(dirname "$OUT")"

fail(){ echo "GV7 RESULT=FAIL $1" | tee "$OUT"; exit 1; }
pass(){ echo "GV7 RESULT=PASS" | tee "$OUT"; exit 0; }

adb get-state >/dev/null 2>&1 || fail "(no device)"
adb shell pm path "$PKG" >/dev/null 2>&1 || fail "(app not installed)"
T="$(adb shell toybox date +%F 2>/dev/null | tr -d '\r')"

adb shell run-as "$PKG" sh <<'IN'
set -eu
mkdir -p app/locks files
echo "date,feature_schema_version,delivered,opened,open_rate" > app/locks/daily_notif_engagement.header
echo "wrong,header,please,replace" > files/daily_notification_engagement.csv
t="$(toybox date +%F)"
echo "ts,event,notif_id" > files/notification_log.csv
echo "$t,POSTED,gv7-a"  >> files/notification_log.csv
echo "$t,CLICKED,gv7-a" >> files/notification_log.csv
IN

adb shell logcat -c >/dev/null 2>&1 || true
adb shell cmd activity broadcast -n "$RCV" -a "$ACT" --receiver-foreground --receiver-include-background --include-stopped-packages >/dev/null 2>&1 || true

i=0; rowA=""
while [ $i -lt 10 ]; do
  rowA="$(adb exec-out run-as "$PKG" awk -F, -v d="$T" 'NR>1&&$1==d{print;exit}' "$DAILY" 2>/dev/null | tr -d '\r' || true)"
  [ -n "$rowA" ] && break
  sleep 0.5; i=$((i+1))
done
[ -z "$rowA" ] || fail "(phaseA: row should not be written when header is wrong: $rowA)"

tools/fix/clear_notif_engagement_sentinels.sh

adb shell run-as "$PKG" sh <<'IN'
set -eu
d="files/daily_notification_engagement.csv"
gold="date,feature_schema_version,delivered,opened,open_rate"
{ echo "$gold"; toybox tail -n +2 "$d" 2>/dev/null || true; } > "$d.new"
toybox mv "$d.new" "$d"
IN

adb shell cmd activity broadcast -n "$RCV" -a "$ACT" --receiver-foreground --receiver-include-background --include-stopped-packages >/dev/null 2>&1 || true

i=0; rowB=""
while [ $i -lt 10 ]; do
  rowB="$(adb exec-out run-as "$PKG" awk -F, -v d="$T" 'NR>1&&$1==d{print;exit}' "$DAILY" 2>/dev/null | tr -d '\r' || true)"
  [ -n "$rowB" ] && break
  sleep 0.5; i=$((i+1))
done

[ "$rowB" = "$T,1,1,1,1.000000" ] || fail "(phaseB: row=$rowB)"
pass
