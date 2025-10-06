#!/bin/sh
set -eu

PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/app_usage_by_category/ee1.txt"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "EE-1 RESULT=FAIL (no device/emulator)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "EE-1 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

US_DECL=$(adb shell dumpsys package "$PKG" 2>/dev/null | tr -d '\r' | grep -Fq "android.permission.PACKAGE_USAGE_STATS" && echo yes || echo no)
US_MODE=$(adb shell cmd appops get "$PKG" android:get_usage_stats 2>/dev/null | tr -d '\r' | awk -F': ' '/GET_USAGE_STATS|android:get_usage_stats|mode=/{m=$2} END{gsub(/;.*$/,"",m); gsub(/^mode=/,"",m); print m}')

S=0
[ "$US_DECL" = yes ] || S=1
echo "${US_MODE:-none}" | grep -Eq '^(allow|foreground)$' || S=1

{
echo "EE-1 USAGE_STATS: DECLARED=$US_DECL MODE=${US_MODE:-none}"
} | tee "$OUT" >/dev/null

if [ "$S" -eq 0 ]; then
echo "EE-1 RESULT=PASS" | tee -a "$OUT"; exit 0
else
echo "EE-1 RESULT=FAIL" | tee -a "$OUT"; exit 1
fi
