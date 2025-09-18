#!/bin/bash
PKG="com.nick.myrecoverytracker"
adb get-state >/dev/null 2>&1 || { echo "WM-LOCK RESULT: FAIL (no device/emulator)"; exit 2; }
adb shell pm path "$PKG"  >/dev/null 2>&1 || { echo "WM-LOCK RESULT: FAIL (app not installed)"; exit 3; }
SNAP="$(adb shell dumpsys jobscheduler 2>/dev/null | sed -n "/$PKG\/androidx.work.impl.background.systemjob.SystemJobService/,+12p" | tr -d '\r')"
[ -n "$SNAP" ] || { echo "WM-LOCK RESULT: FAIL (no WorkManager jobs visible)"; exit 4; }
echo "$SNAP"
echo "WM-LOCK RESULT: PASS"
exit 0
