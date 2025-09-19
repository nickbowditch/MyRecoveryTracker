#!/usr/bin/env bash
PKG="com.nick.myrecoverytracker"
DAYS="${DAYS:-14}"
adb shell am broadcast -a "$PKG".ACTION_RUN_SLEEP_ROLLUP --ei backfill_days "$DAYS" -n "$PKG"/.TriggerReceiver >/dev/null
sleep 2
adb shell am broadcast -a "$PKG".ACTION_RUN_SLEEP_ROLLUP --ei backfill_days "$DAYS" -n "$PKG"/.TriggerReceiver >/dev/null
sleep 1
echo "Sleep backfill requested for last $DAYS day(s)"
