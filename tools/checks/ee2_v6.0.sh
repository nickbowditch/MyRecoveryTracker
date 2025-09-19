#!/bin/bash
PKG="com.nick.myrecoverytracker"
S=0

adb get-state >/dev/null 2>&1 || { echo "EE-2 RESULT: FAIL (no device/emulator)"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "EE-2 RESULT: FAIL (app not installed)"; exit 3; }

OUT=$(adb shell dumpsys jobscheduler | grep -F "$PKG" | grep -F "UNLOCK_ROLLUP" | tr -d '\r')
if [ -n "$OUT" ]; then S=0; else S=1; fi

echo "EE-2 JOBSCHEDULER: ${OUT:-none}"
if [ "$S" -eq 0 ]; then echo "EE-2 RESULT: PASS"; exit 0; else echo "EE-2 RESULT: FAIL"; exit 1; fi
