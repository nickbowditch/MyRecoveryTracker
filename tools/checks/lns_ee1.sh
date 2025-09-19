#!/bin/bash
PKG="com.nick.myrecoverytracker"

get_mode() {
  adb shell cmd appops get "$PKG" GET_USAGE_STATS 2>/dev/null \
  | tr -d '\r' \
  | grep -Eo '(allow|ignore|deny)' \
  | head -n1
}

mode="$(get_mode)"
[ -z "$mode" ] && mode="$(
  adb shell appops get "$PKG" GET_USAGE_STATS 2>/dev/null \
  | tr -d '\r' \
  | grep -Eo '(allow|ignore|deny)' \
  | head -n1
)"
[ -z "$mode" ] && mode="$(adb shell appops get "$PKG" android:get_usage_stats 2>/dev/null | tr -d '\r' | grep -Eo '(allow|ignore|deny)' | head -n1)"
[ -z "$mode" ] && mode="unknown"

ar_req="$(adb shell dumpsys package "$PKG" 2>/dev/null | grep -c 'android.permission.ACTIVITY_RECOGNITION')"
ar_ok="n/a"
if [ "$ar_req" -gt 0 ]; then
  ar_ok="$(adb shell dumpsys package "$PKG" 2>/dev/null \
    | awk '/android.permission.ACTIVITY_RECOGNITION/{f=1} f&&/granted=/{print; exit}' \
    | grep -q 'granted=true' && echo granted || echo not_granted)"
fi

if [ "$mode" = "allow" ] && { [ "$ar_ok" = "n/a" ] || [ "$ar_ok" = "granted" ]; }; then
  echo "LNS EE-1 RESULT=PASS (usage_stats=$mode, activity_recognition=$ar_ok)"
  exit 0
else
  echo "LNS EE-1 RESULT=FAIL (usage_stats=$mode, activity_recognition=$ar_ok)"
  exit 1
fi
