#!/bin/bash
PKG="com.nick.myrecoverytracker"
S=0

adb get-state >/dev/null 2>&1 || { echo "EE-3 RESULT: FAIL (no device/emulator)"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "EE-3 RESULT: FAIL (app not installed)"; exit 3; }

adb shell dumpsys deviceidle force-idle >/dev/null 2>&1
sleep 5
OUT=$(adb shell dumpsys jobscheduler | grep -F "$PKG" | grep -F "UNLOCK_ROLLUP" | tr -d '\r')

if [ -n "$OUT" ]; then S=0; else S=1; fi

echo "EE-3 DEVICEIDLE JOBSCHEDULER: ${OUT:-none}"
if [ "$S" -eq 0 ]; then echo "EE-3 RESULT: PASS"; exit 0; else echo "EE-3 RESULT: FAIL"; exit 1; fi
