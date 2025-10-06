#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
CSV="files/daily_notification_latency.csv"
OUT="evidence/v6.0/notification_latency/at1.txt"
mkdir -p "$(dirname "$OUT")"

fail(){ echo "AT-1 RESULT=FAIL $1" | tee "$OUT"; exit 1; }

adb get-state >/dev/null 2>&1 || fail "(no device)"
adb shell pm path "$PKG" >/dev/null 2>&1 || fail "(app not installed)"

BEFORE="$(adb exec-out run-as "$PKG" sh -c "md5sum \"$CSV\" 2>/dev/null" | awk '{print $1}' || true)"

adb shell am broadcast -a com.nick.myrecoverytracker.ACTION_RUN_LATENCY_ROLLUP >/dev/null 2>&1 || fail "(broadcast failed)"
sleep 2

AFTER="$(adb exec-out run-as "$PKG" sh -c "md5sum \"$CSV\" 2>/dev/null" | awk '{print $1}' || true)"

if [ -z "$AFTER" ]; then
  fail "(csv missing after broadcast)"
fi

if [ "$BEFORE" = "$AFTER" ]; then
  echo "AT-1 RESULT=FAIL (csv unchanged)" | tee "$OUT"
  exit 2
fi

echo "AT-1 RESULT=PASS" | tee "$OUT"
exit 0
