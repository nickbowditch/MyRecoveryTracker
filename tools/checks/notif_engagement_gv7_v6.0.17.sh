#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
RAW="files/notification_log.csv"
DAILY="files/daily_notification_engagement.csv"
LOCK1="app/locks/daily_notif_engagement.header"
LOCK2="app/locks/daily_notif_engagement.head"
RCV="$PKG/.TriggerReceiver"
ACT_ROLLUP="$PKG.ACTION_RUN_NOTIFICATION_ROLLUP"
ACT_ENGAGE="$PKG.ACTION_RUN_NOTIFICATION_ENGAGEMENT_ROLLUP"
OUT="evidence/v6.0/notification_engagement/gv7.17.txt"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "GV7 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG"  >/dev/null 2>&1 || { echo "GV7 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

T="$(adb shell date +%F | tr -d '\r')"

adb shell run-as "$PKG" sh <<'IN'
set -eu
raw="files/notification_log.csv"
daily="files/daily_notification_engagement.csv"
lock1="app/locks/daily_notif_engagement.header"
lock2="app/locks/daily_notif_engagement.head"
gold="date,feature_schema_version,delivered,opened,open_rate"
mkdir -p app/locks files
# write BOTH possible lock files so whichever the app reads is present
echo "$gold" > "$lock1"
echo "$gold" > "$lock2"
# deliberately wrong daily header to test self-heal
echo "wrong,header,please,replace" > "$daily"
# seed raw with today's POSTED/CLICKED
t="$(toybox date +%F)"
echo "ts,event,notif_id" > "$raw"
echo "$t,POSTED,gv7-a"  >> "$raw"
echo "$t,CLICKED,gv7-a" >> "$raw"
IN

# nuke logs and fire both actions (order covers either mapping)
adb shell logcat -c >/dev/null 2>&1 || true
adb shell cmd activity broadcast -n "$RCV" -a "$ACT_ENGAGE" --receiver-foreground --receiver-include-background --include-stopped-packages >/dev/null 2>&1 || true
adb shell cmd activity broadcast -n "$RCV" -a "$ACT_ROLLUP" --receiver-foreground --receiver-include-background --include-stopped-packages >/dev/null 2>&1 || true

# wait/poll for header fix + row
i=0
hdr=""
row=""
while [ $i -lt 10 ]; do
  hdr="$(adb exec-out run-as "$PKG" head -n1 "$DAILY" 2>/dev/null | tr -d '\r' || true)"
  row="$(adb exec-out run-as "$PKG" awk -F, -v d="$T" 'NR>1&&$1==d{print;exit}' "$DAILY" 2>/dev/null | tr -d '\r' || true)"
  [ "$hdr" = "date,feature_schema_version,delivered,opened,open_rate" ] && [ -n "$row" ] && break
  sleep 0.5
  i=$((i+1))
done

[ "$hdr" = "date,feature_schema_version,delivered,opened,open_rate" ] || { echo "GV7 RESULT=FAIL (bad header: $hdr)" | tee "$OUT"; exit 1; }

case "$row" in
"$T",1,1,1,1.000000) echo "GV7 RESULT=PASS" | tee "$OUT" ;;
*) echo "GV7 RESULT=FAIL (row=$row)" | tee "$OUT"; exit 1 ;;
esac
