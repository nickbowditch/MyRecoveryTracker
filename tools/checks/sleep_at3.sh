#!/bin/bash
PKG="com.nick.myrecoverytracker"

adb shell am broadcast -a "$PKG".ACTION_RUN_SLEEP_ROLLUP -n "$PKG"/.TriggerReceiver >/dev/null 2>&1
sleep 1

TMP="$(adb exec-out run-as "$PKG" sh -c 'ls -1 files/daily_sleep_summary.csv.tmp 2>/dev/null' | tr -d '\r')"
[ -n "$TMP" ] && { echo "Sleep AT-3 RESULT=FAIL"; exit 1; }

CSV1="$(adb exec-out run-as "$PKG" cat files/daily_sleep_summary.csv 2>/dev/null | tr -d '\r')"
CSV2="$(adb exec-out run-as "$PKG" cat files/daily_sleep_summary.csv 2>/dev/null | tr -d '\r')"
[ -z "$CSV1" ] && { echo "Sleep AT-3 RESULT=FAIL"; exit 1; }
[ "$CSV1" != "$CSV2" ] && { echo "Sleep AT-3 RESULT=FAIL"; exit 1; }

BAD="$(awk -F, '
NR==1 { ok=($0=="date,sleep_time,wake_time,duration_hours"); if(!ok){print "BAD"; exit} next }
$0=="" { next }
$1=="date" { next }
(NF!=4)||($1!~/^[0-9]{4}-[0-9]{2}-[0-9]{2}$/) { bad=1 }
END { if (bad) print "BAD" }
' <<<"$CSV1")"

[ -z "$BAD" ] && echo "Sleep AT-3 RESULT=PASS" || { echo "Sleep AT-3 RESULT=FAIL"; exit 1; }
