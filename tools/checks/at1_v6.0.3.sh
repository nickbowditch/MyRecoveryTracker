#!/bin/sh
PKG="com.nick.myrecoverytracker"
CSV_DAILY="files/daily_unlocks.csv"
CSV_RAW="files/unlock_log.csv"
ACT="$PKG.ACTION_RUN_UNLOCK_ROLLUP"
CMP="$PKG/.TriggerReceiver"
OUT="evidence/v6.0/unlocks/at1.console.3.txt"

adb get-state >/dev/null 2>&1 || { echo "AT-1 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "AT-1 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

now="$(adb shell 'TZ=UTC date +%FT%T' | tr -d '\r')"

adb exec-out run-as "$PKG" sh -c "
mkdir -p files
[ -f $CSV_RAW ] || echo 'ts,event' > $CSV_RAW
[ -f $CSV_DAILY ] || echo 'date,feature_schema_version,daily_unlocks' > $CSV_DAILY
"

h0="$(adb exec-out run-as "$PKG" sha1sum $CSV_DAILY | awk '{print $1}')"

adb exec-out run-as "$PKG" sh -c "echo '$now,UNLOCK' >> $CSV_RAW"
adb shell am broadcast -a "$ACT" -n "$CMP" >/dev/null 2>&1 || { echo "AT-1 RESULT=FAIL (broadcast)" | tee "$OUT"; exit 4; }
sleep 2

h1="$(adb exec-out run-as "$PKG" sha1sum $CSV_DAILY | awk '{print $1}')"

if [ "$h0" != "$h1" ]; then
echo "AT-1 RESULT=PASS" | tee "$OUT"; exit 0
else
echo "AT-1 RESULT=FAIL" | tee "$OUT"; exit 1
fi
