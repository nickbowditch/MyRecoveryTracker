#!/bin/bash
PKG="com.nick.myrecoverytracker"
adb get-state >/dev/null 2>&1 || { echo "DI-1 RESULT=FAIL (no device)"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "DI-1 RESULT=FAIL (app not installed)"; exit 3; }
LOCK="$(tr -d $'\r' < app/locks/daily_metrics.header 2>/dev/null)"
[ -n "$LOCK" ] || { echo "DI-1 RESULT=FAIL (missing lock header)"; exit 4; }
HEAD="$(adb exec-out run-as "$PKG" sh -c 'head -n 1 files/daily_unlocks.csv 2>/dev/null' | tr -d $'\r')"
[ -n "$HEAD" ] || { echo "DI-1 RESULT=FAIL (missing csv)"; exit 5; }
if [ "$HEAD" = "$LOCK" ]; then
echo "DI-1 RESULT=PASS"
exit 0
else
echo "DI-1 RESULT=FAIL"
echo "LOCK=$LOCK"
echo "HEAD=$HEAD"
exit 1
fi
