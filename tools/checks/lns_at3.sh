#!/usr/bin/env bash
PKG="com.nick.myrecoverytracker"
F="files/daily_late_screen.csv"

adb shell am broadcast -a "$PKG".ACTION_RUN_LATE_SCREEN_ROLLUP -n "$PKG"/.TriggerReceiver >/dev/null 2>&1 || true
sleep 1

TMP="$(adb exec-out run-as "$PKG" sh -c 'ls -1 files/daily_late_screen.csv.tmp 2>/dev/null' | tr -d '\r')"
[ -n "$TMP" ] && { echo "LNS AT-3 RESULT=FAIL"; exit 1; }

CSV1="$(adb exec-out run-as "$PKG" cat "$F" 2>/dev/null | tr -d '\r')"
CSV2="$(adb exec-out run-as "$PKG" cat "$F" 2>/dev/null | tr -d '\r')"
[ -z "$CSV1" ] && { echo "LNS AT-3 RESULT=FAIL"; exit 1; }
[ "$CSV1" != "$CSV2" ] && { echo "LNS AT-3 RESULT=FAIL"; exit 1; }

BAD="$(awk -F, '
NR==1 { ok=($0=="date,late_minutes"); if(!ok){print "BAD"; exit} next }
$0=="" { next }
$1=="date" { next }
(NF!=2)||($1!~/^[0-9]{4}-[0-9]{2}-[0-9]{2}$/)||($2!~/^[0-9]+$/) { bad=1 }
END { if (bad) print "BAD" }
' <<<"$CSV1")"

[ -z "$BAD" ] && echo "LNS AT-3 RESULT=PASS" || { echo "LNS AT-3 RESULT=FAIL"; exit 1; }
