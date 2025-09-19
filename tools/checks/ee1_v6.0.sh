#!/bin/bash
PKG="com.nick.myrecoverytracker"
S=0
adb get-state >/dev/null 2>&1 || { echo "EE-1 RESULT: FAIL (no device/emulator)"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "EE-1 RESULT: FAIL (app not installed)"; exit 3; }
US_DECL=$(adb shell dumpsys package "$PKG" 2>/dev/null | tr -d $'\r' | grep -Fq "android.permission.PACKAGE_USAGE_STATS" && echo yes || echo no)
US_MODE=$(adb shell cmd appops get "$PKG" android:get_usage_stats 2>/dev/null | tr -d $'\r' | awk -F': ' '/GET_USAGE_STATS|android:get_usage_stats|mode=/{m=$2} END{gsub(/;.*$/,"",m); gsub(/^mode=/,"",m); print m}')
[ "$US_DECL" = yes ] || S=1
echo "${US_MODE:-none}" | grep -Eq '^(allow|foreground)$' || S=1
AR_DECL=$(adb shell dumpsys package "$PKG" 2>/dev/null | tr -d $'\r' | grep -Fq "android.permission.ACTIVITY_RECOGNITION" && echo yes || echo no)
if [ "$AR_DECL" = yes ]; then
AR_GR=$(adb shell dumpsys package "$PKG" 2>/dev/null | tr -d $'\r' | awk '/android.permission.ACTIVITY_RECOGNITION:/ {print}' | grep -Fq "granted=true" && echo yes || echo no)
[ "$AR_GR" = yes ] || S=1
else
AR_GR=skip
fi
echo "EE-1 USAGE_STATS: DECLARED=$US_DECL MODE=${US_MODE:-none}"
echo "EE-1 ACTIVITY_RECOGNITION: DECLARED=$AR_DECL GRANTED=${AR_GR:-skip}"
if [ "$S" -eq 0 ]; then echo "EE-1 RESULT: PASS"; exit 0; else echo "EE-1 RESULT: FAIL"; exit 1; fi
