#!/usr/bin/env bash
PKG="com.nick.myrecoverytracker"
F="files/daily_late_screen.csv"

hash_file() {
  adb exec-out run-as "$PKG" sh -c '
    mkdir -p files
    [ -f "'"$F"'" ] || printf "date,late_minutes\n" > "'"$F"'"
    md5sum "'"$F"'" 2>/dev/null | awk "{print \$1}"
  ' | tr -d '\r'
}

H0="$(hash_file)"

adb shell am broadcast -a "$PKG".ACTION_RUN_UNLOCK_ROLLUP       -n "$PKG"/.TriggerReceiver >/dev/null 2>&1
sleep 1
adb shell am broadcast -a "$PKG".ACTION_RUN_NOTIFICATION_ROLLUP  -n "$PKG"/.TriggerReceiver >/dev/null 2>&1
sleep 1
adb shell am broadcast -a "$PKG".ACTION_RUN_MOVEMENT_ROLLUP     -n "$PKG"/.TriggerReceiver >/dev/null 2>&1
sleep 1

H1="$(hash_file)"

if [ "$H1" != "$H0" ]; then
  echo "LNS AT-1 RESULT=FAIL"
  exit 1
fi
echo "LNS AT-1 RESULT=PASS"
