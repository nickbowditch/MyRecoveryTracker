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
y=$(toybox date -d "@$((now-86400))" +%F)
t=$(toybox date -d "@$now" +%F)

# NOTE: first column is DATE-ONLY (no time)
echo "ts,event,notif_id" > "$raw"
echo "$y,POSTED,tc2-y"    >> "$raw"
echo "$y,CLICKED,tc2-y"   >> "$raw"
echo "$t,POSTED,tc2-t"    >> "$raw"
echo "$t,CLICKED,tc2-t"   >> "$raw"
IN
