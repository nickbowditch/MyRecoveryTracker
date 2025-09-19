#!/bin/bash
PKG="com.nick.myrecoverytracker"

FOUND=""
adb shell am broadcast -a "$PKG".ACTION_RUN_SLEEP_ROLLUP -n "$PKG"/.TriggerReceiver >/dev/null 2>&1
for i in 1 2 3 4 5 6 7 8; do
  L="$(adb exec-out run-as "$PKG" sh -c 'ls -1 files/.lock files/.LOCK files/.lck files/.LCK files/daily_sleep_summary.csv.lock files/daily_sleep_summary.csv.tmp 2>/dev/null' | tr -d '\r')"
  [ -n "$L" ] && FOUND="$L"
  sleep 0.5
done

if [ -z "$FOUND" ]; then
  echo "Sleep AT-2 RESULT=PASS"
  exit 0
else
  echo "Sleep AT-2 RESULT=FAIL"
  printf "%s\n" "$FOUND"
  exit 1
fi
