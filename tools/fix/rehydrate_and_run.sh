#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
RCV="$PKG/.TriggerReceiver"
ACT="$PKG.ACTION_RUN_NOTIFICATION_ROLLUP"

adb shell am start -n "$PKG"/.MainActivity >/dev/null 2>&1 || true
sleep 1

adb shell run-as "$PKG" sh <<'IN'
set -eu
mkdir -p files app/locks
HDR="date,feature_schema_version,delivered,opened,open_rate"
echo "$HDR" > app/locks/daily_notif_engagement.header
[ -f files/daily_notification_engagement.csv ] || echo "$HDR" > files/daily_notification_engagement.csv
t="$(toybox date +%F)"
{
  echo "ts,event,notif_id"
  echo "$t,POSTED,rehydrate-a"
  echo "$t,POSTED,rehydrate-b"
  echo "$t,CLICKED,rehydrate-a"
} > files/notification_log.csv
IN

adb shell cmd activity broadcast -a "$ACT" -n "$RCV" \
  --receiver-foreground --receiver-include-background --include-stopped-packages >/dev/null 2>&1 || true
sleep 2

TODAY="$(adb shell date +%F | tr -d '\r')"
adb exec-out run-as "$PKG" toybox grep -m1 "^$TODAY," files/daily_notification_engagement.csv | tr -d '\r' || true
