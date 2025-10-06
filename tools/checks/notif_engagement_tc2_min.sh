#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
RAW="files/notification_log.csv"
DAILY="files/daily_notification_engagement.csv"
RCV="$PKG/.TriggerReceiver"
A1="$PKG.ACTION_RUN_NOTIFICATION_ENGAGEMENT_ROLLUP"

EPOCH="$(adb shell toybox date +%s | tr -d '\r')"
Y_DATE="$(adb shell toybox date -d "@$((EPOCH-86400))" +%F | tr -d '\r')"
T_DATE="$(adb shell date +%F | tr -d '\r')"

TS_Y="$(adb shell toybox date -d "$Y_DATE 23:59:00" '+%Y-%m-%d %H:%M:%S' | tr -d '\r')"
TS_T="$(adb shell toybox date -d "$T_DATE 00:01:00" '+%Y-%m-%d %H:%M:%S' | tr -d '\r')"

adb exec-out run-as "$PKG" sh -c "
  echo 'ts,event,notif_id' > $RAW
  echo '$TS_Y,DELIVERED,tc2-y' >> $RAW
  echo '$TS_Y,OPENED,tc2-y'    >> $RAW
  echo '$TS_T,DELIVERED,tc2-t' >> $RAW
  echo '$TS_T,OPENED,tc2-t'    >> $RAW
"

adb shell cmd activity broadcast -n "$RCV" -a "$A1" --receiver-foreground --user 0 >/dev/null 2>&1

sleep 2

adb exec-out run-as "$PKG" awk -F, -v y="$Y_DATE" -v t="$T_DATE" \
  'NR==1 || $1==y || $1==t' "$DAILY" | tr -d '\r'
