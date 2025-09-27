#!/bin/sh
PKG="com.nick.myrecoverytracker"
TODAY="$(adb shell date +%F | tr -d '\r')"

S1="$(adb exec-out run-as "$PKG" cat files/daily_sleep_summary.csv 2>/dev/null | tr -d '\r' || printf "")"
S2="$(adb exec-out run-as "$PKG" cat files/daily_sleep_duration.csv 2>/dev/null | tr -d '\r' || printf "")"

BAD1="$(printf '%s\n' "$S1" | awk -F, -v t="$TODAY" 'NR>1 && $1>t{print}')"
BAD2="$(printf '%s\n' "$S2" | awk -F, -v t="$TODAY" 'NR>1 && $1>t{print}')"

if [ -z "$BAD1$BAD2" ]; then
echo "TC-3 RESULT=PASS"
exit 0
else
echo "TC-3 RESULT=FAIL"
[ -n "$BAD1" ] && { echo "-- future rows in daily_sleep_summary.csv --"; printf '%s\n' "$BAD1"; }
[ -n "$BAD2" ] && { echo "-- future rows in daily_sleep_duration.csv --"; printf '%s\n' "$BAD2"; }
exit 1
fi
