#!/bin/bash
PKG="com.nick.myrecoverytracker"
S=0

adb kill-server >/dev/null 2>&1; adb start-server >/dev/null 2>&1
for i in {1..30}; do
st="$(adb get-state 2>/dev/null | tr -d $'\r')"
[ "$st" = "device" ] && break
sleep 1
done
[ "$st" = "device" ] || { echo "TC-1 RESULT: FAIL (no device/emulator)"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "TC-1 RESULT: FAIL (app not installed)"; exit 3; }

HOST_EPOCH="$(date +%s)"
DEV_EPOCH="$(adb shell toybox date +%s 2>/dev/null | tr -d $'\r')"
[ -n "$DEV_EPOCH" ] || { echo "TC-1 RESULT: FAIL (device epoch unreadable)"; exit 4; }

D=$(( HOST_EPOCH - DEV_EPOCH ))
[ $D -lt 0 ] && D=$(( -D ))
MAX=300

echo "TC-1 HOST_EPOCH=$HOST_EPOCH DEV_EPOCH=$DEV_EPOCH DIFF_SECS=$D"

if [ $D -le $MAX ]; then
echo "TC-1 RESULT: PASS"
exit 0
else
echo "TC-1 RESULT: FAIL (clock drift > ${MAX}s)"
exit 1
fi
