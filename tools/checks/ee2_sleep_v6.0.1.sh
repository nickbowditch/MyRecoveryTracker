#!/bin/bash
PKG="com.nick.myrecoverytracker"
S=0
adb kill-server >/dev/null 2>&1; adb start-server >/dev/null 2>&1
for i in {1..30}; do
st="$(adb get-state 2>/dev/null | tr -d '\r')"
[ "$st" = "device" ] && break
sleep 1
done
[ "$st" = "device" ] || { echo "EE-2 RESULT: FAIL (no device/emulator)"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "EE-2 RESULT: FAIL (app not installed)"; exit 3; }

OUT="$(adb shell dumpsys jobscheduler 2>/dev/null | sed -n "/$PKG\/androidx.work.impl.background.systemjob.SystemJobService/,+12p" | tr -d '\r')"
[ -n "$OUT" ] || S=1

echo "EE-2 JOBSCHEDULER:${OUT:+ }${OUT:-none}"
if [ "$S" -eq 0 ]; then echo "EE-2 RESULT: PASS"; exit 0; else echo "EE-2 RESULT: FAIL"; exit 1; fi
