#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
adb get-state >/dev/null 2>&1 || exit 2
adb shell pm path "$PKG" >/dev/null 2>&1 || exit 3
adb shell run-as "$PKG" sh <<'IN'
set -eu
raw="files/notification_log.csv"
csv="files/daily_notification_engagement.csv"
lock="app/locks/daily_notif_engagement.head"
hdr="date,feature_schema_version,delivered,opened,open_rate"

mkdir -p app/locks files
[ -f "$lock" ] || echo "$hdr" > "$lock"
[ -f "$csv" ]  || echo "$hdr" > "$csv"

now=$(toybox date +%s)
y_date=$(toybox date -d "@$((now-86400))" +%F)
t_date=$(toybox date -d "@$now" +%F)
ts_y="$y_date 23:59:00"
ts_t="$t_date 00:01:00"

echo "ts,event,notif_id" > "$raw"
echo "$ts_y,POSTED,tc2-y"  >> "$raw"
echo "$ts_y,CLICKED,tc2-y" >> "$raw"
echo "$ts_t,POSTED,tc2-t"  >> "$raw"
echo "$ts_t,CLICKED,tc2-t" >> "$raw"
IN
