#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
RAW="files/notification_log.csv"
DAILY="files/daily_notification_engagement.csv"
LOCK="app/locks/daily_notif_engagement.head"
RCV="$PKG/.TriggerReceiver"
ACT="$PKG.ACTION_RUN_NOTIFICATION_ENGAGEMENT_ROLLUP"
OUT="evidence/v6.0/notification_engagement/tc1.15.txt"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "TC1 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG"  >/dev/null 2>&1 || { echo "TC1 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

T="$(adb shell date +%F | tr -d '\r')"

adb shell run-as "$PKG" sh <<'IN'
set -eu
raw="files/notification_log.csv"
daily="files/daily_notification_engagement.csv"
lock="app/locks/daily_notif_engagement.head"
hdr="date,feature_schema_version,delivered,opened,open_rate"
mkdir -p app/locks files
[ -f "$lock" ] || echo "$hdr" > "$lock"
[ -f "$daily" ] || echo "$hdr" > "$daily"
t="$(toybox date +%F)"
echo "ts,event,notif_id" > "$raw"
echo "$t,POSTED,tc1-a"  >> "$raw"
echo "$t,POSTED,tc1-b"  >> "$raw"
echo "$t,CLICKED,tc1-a" >> "$raw"
IN

adb shell cmd activity broadcast -n "$RCV" -a "$ACT" --receiver-foreground --user 0 >/dev/null 2>&1 || true
sleep 2

row="$(adb exec-out run-as "$PKG" awk -F, -v d="$T" 'NR>1&&$1==d{print;exit}' "$DAILY" | tr -d '\r')"
case "$row" in
  "$T",1,2,1,0.500000) echo "TC1 RESULT=PASS" | tee "$OUT" ;;
  *) echo "TC1 RESULT=FAIL (row=$row)" | tee "$OUT"; exit 1 ;;
esac
