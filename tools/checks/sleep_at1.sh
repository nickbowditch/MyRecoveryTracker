#!/bin/bash
PKG="com.nick.myrecoverytracker"

hash_file() {
  adb exec-out run-as "$PKG" sh -c '
    f=files/daily_sleep_summary.csv
    mkdir -p files
    [ -f "$f" ] || printf "date,sleep_time,wake_time,duration_hours\n" > "$f"
    md5sum "$f" 2>/dev/null | awk "{print \$1}"
  ' | tr -d '\r'
}

H0="$(hash_file)"

adb shell am broadcast -a "$PKG".ACTION_RUN_UNLOCK_ROLLUP      -n "$PKG"/.TriggerReceiver >/dev/null 2>&1
sleep 1
adb shell am broadcast -a "$PKG".ACTION_RUN_NOTIFICATION_ROLLUP -n "$PKG"/.TriggerReceiver >/dev/null 2>&1
sleep 1
adb shell am broadcast -a "$PKG".ACTION_RUN_MOVEMENT_ROLLUP    -n "$PKG"/.TriggerReceiver >/dev/null 2>&1
sleep 1

H1="$(hash_file)"

if [ "$H1" != "$H0" ]; then
  echo "Sleep AT-1 RESULT=FAIL"
  exit 1
fi

echo "Sleep AT-1 RESULT=PASS"
