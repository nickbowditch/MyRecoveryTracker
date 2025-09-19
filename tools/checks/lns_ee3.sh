#!/usr/bin/env bash
PKG="com.nick.myrecoverytracker"
ACTION="${ACTION_OVERRIDE:?set ACTION_OVERRIDE to your LNS rollup action}"
FILE="files/daily_late_screen.csv"

ensure_file() {
  adb exec-out run-as "$PKG" sh -c '
    mkdir -p files
    [ -f files/daily_late_screen.csv ] || printf "date,late_minutes\n" > files/daily_late_screen.csv
  ' >/dev/null 2>&1
}

hash_now() {
  adb exec-out run-as "$PKG" sh -c '
    [ -f files/daily_late_screen.csv ] && md5sum files/daily_late_screen.csv 2>/dev/null | awk "{print \$1}" || echo NONE
  ' | tr -d "\r"
}

ensure_file
H0="$(hash_now)"

adb shell cmd deviceidle force-idle >/dev/null 2>&1 || true
adb shell cmd deviceidle tempwhitelist "$PKG" >/dev/null 2>&1 || true

adb shell am broadcast --receiver-foreground -a "$ACTION" --ei backfill_days 1 -n "$PKG"/.TriggerReceiver >/dev/null 2>&1 || true

t=0; H1="$H0"
while [ $t -lt 20 ]; do
  sleep 1
  H1="$(hash_now)"
  [ "$H1" != "$H0" ] && break
  t=$((t+1))
done

adb shell cmd deviceidle unforce >/dev/null 2>&1 || true

if [ "$H1" != "$H0" ] && [ "$H1" != "NONE" ]; then
  echo "LNS EE-3 RESULT=PASS"
  exit 0
else
  echo "LNS EE-3 RESULT=FAIL"
  exit 1
fi
