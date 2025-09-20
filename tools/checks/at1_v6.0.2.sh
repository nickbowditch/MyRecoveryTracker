#!/bin/sh
PKG="com.nick.myrecoverytracker"
CSV_DAILY="files/daily_unlocks.csv"
CSV_RAW="files/unlock_log.csv"
ACT="$PKG.ACTION_RUN_UNLOCK_ROLLUP"
CMP="$PKG/.TriggerReceiver"
adb get-state >/dev/null 2>&1 || { echo "AT-1 RESULT=FAIL (no device)"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "AT-1 RESULT=FAIL (app not installed)"; exit 3; }
now="$(adb shell date +%F\ %T | tr -d '\r')"
adb exec-out run-as "$PKG" sh -c '
set -eu
mkdir -p files
[ -f "'"$CSV_RAW"'" ] || printf "ts,event\n" > "'"$CSV_RAW"'"
[ -f "'"$CSV_DAILY"'" ] || printf "date,feature_schema_version,daily_unlocks\n" > "'"$CSV_DAILY"'"
' >/dev/null
h0="$(adb exec-out run-as "$PKG" sh -c 'cat "'"$CSV_DAILY"'" 2>/dev/null | sha1sum 2>/dev/null | awk "{print \$1}"' || true)"
adb exec-out run-as "$PKG" sh -c 'printf "%s,UNLOCK\n" "'"$now"'" >> "'"$CSV_RAW"'"' >/dev/null
adb shell am broadcast -a "$ACT" -n "$CMP" >/dev/null 2>&1 || { echo "AT-1 RESULT=FAIL (broadcast)"; exit 4; }
sleep 2
h1="$(adb exec-out run-as "$PKG" sh -c 'cat "'"$CSV_DAILY"'" 2>/dev/null | sha1sum 2>/dev/null | awk "{print \$1}"' || true)"
adb exec-out run-as "$PKG" sh -c '
in="'"$CSV_RAW"'"; tmp="${in}.tmp.$$"; ts="'"$now"'"
awk -F, -v ts="$ts" "NR==1{print;next}{ if(\$1!=ts) print }" "$in" > "$tmp" && mv "$tmp" "$in"
' >/dev/null || true
[ -n "$h0" ] && [ -n "$h1" ] && [ "$h0" != "$h1" ] && { echo "AT-1 RESULT=PASS"; exit 0; }
echo "AT-1 RESULT=FAIL"; exit 1
