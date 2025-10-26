#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
RAW="files/notification_log.csv"
DAILY="files/daily_notification_engagement.csv"
LOCK="app/locks/daily_notif_engagement.header"   
RCV="$PKG/.TriggerReceiver"
ACT="$PKG.ACTION_RUN_NOTIFICATION_ROLLUP"         
OUT="evidence/v6.0/notification_engagement/gv7.16.txt"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "GV7 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG"  >/dev/null 2>&1 || { echo "GV7 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

T="$(adb shell date +%F | tr -d '\r')"

adb shell run-as "$PKG" sh <<'IN'
set -eu
raw="files/notification_log.csv"
daily="files/daily_notification_engagement.csv"
lock="app/locks/daily_notif_engagement.header"
gold="date,feature_schema_version,delivered,opened,open_rate"
mkdir -p app/locks files
echo "$gold" > "$lock"
echo "wrong,header,please,replace" > "$daily"
t="$(toybox date +%F)"
echo "ts,event,notif_id" > "$raw"
echo "$t,POSTED,gv7-a"  >> "$raw"
echo "$t,CLICKED,gv7-a" >> "$raw"
IN

adb shell cmd activity broadcast -n "$RCV" -a "$ACT" --receiver-foreground --receiver-include-background --include-stopped-packages >/dev/null 2>&1 || true
sleep 2

hdr="$(adb exec-out run-as "$PKG" head -n1 "$DAILY" | tr -d '\r')"
row="$(adb exec-out run-as "$PKG" awk -F, -v d="$T" 'NR>1&&$1==d{print;exit}' "$DAILY" | tr -d '\r')"

[ "$hdr" = "date,feature_schema_version,delivered,opened,open_rate" ] || { echo "GV7 RESULT=FAIL (bad header: $hdr)" | tee "$OUT"; exit 1; }
case "$row" in
"$T",1,1,1,1.000000) echo "GV7 RESULT=PASS" | tee "$OUT" ;;
*) echo "GV7 RESULT=FAIL (row=$row)" | tee "$OUT"; exit 1 ;;
esac
