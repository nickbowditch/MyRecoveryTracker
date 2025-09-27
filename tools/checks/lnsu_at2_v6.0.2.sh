#!/bin/sh
set -eu
APP="${APP:-com.nick.myrecoverytracker}"
RCV="$APP/.TriggerReceiver"
ACT="com.nick.myrecoverytracker.ACTION_RUN_LNS_ROLLUP"
OUT="evidence/v6.0/lnsu/at2.txt"

adb exec-out run-as "$APP" cat files/daily_late_night_screen_usage.csv \
> evidence/v6.0/lnsu/dlnsu.first.csv

adb shell cmd activity broadcast -n "$RCV" -a "$ACT" --receiver-foreground --user 0 >/dev/null
adb exec-out run-as "$APP" cat files/daily_late_night_screen_usage.csv \
> evidence/v6.0/lnsu/dlnsu.second.csv

adb shell cmd activity broadcast -n "$RCV" -a "$ACT" --receiver-foreground --user 0 >/dev/null
adb exec-out run-as "$APP" cat files/daily_late_night_screen_usage.csv \
> evidence/v6.0/lnsu/dlnsu.third.csv

h1=$(shasum -a 1 evidence/v6.0/lnsu/dlnsu.second.csv | awk '{print $1}')
h2=$(shasum -a 1 evidence/v6.0/lnsu/dlnsu.third.csv | awk '{print $1}')

[ "$h1" = "$h2" ] && { echo "AT2 RESULT=PASS" | tee "$OUT"; exit 0; }
echo "AT2 RESULT=FAIL (hash mismatch)" | tee "$OUT"; exit 1
