#!/usr/bin/env bash
PKG="com.nick.myrecoverytracker"

has_boot="$(adb shell dumpsys package "$PKG" 2>/dev/null | awk '/Receivers:/{r=1} r&&/BOOT_COMPLETED/{print; exit}' | wc -l | tr -d '[:space:]')"
has_repl="$(adb shell dumpsys package "$PKG" 2>/dev/null | awk '/Receivers:/{r=1} r&&/PACKAGE_REPLACED/{print; exit}' | wc -l | tr -d '[:space:]')"

adb shell am broadcast -a android.intent.action.BOOT_COMPLETED -p "$PKG" >/dev/null 2>&1
sleep 1
adb shell am broadcast -a android.intent.action.PACKAGE_REPLACED --es "android.intent.extra.PACKAGES" "$PKG" >/dev/null 2>&1
sleep 2

jobs_hit="$(adb shell dumpsys jobscheduler 2>/dev/null | grep -qi "$PKG" && echo hit || true)"
alarms_hit="$(adb shell dumpsys alarm 2>/dev/null | grep -qi "$PKG" && echo hit || true)"
wm_hit="$(adb shell dumpsys activity services 2>/dev/null | grep -qiE "$PKG|workmanager|androidx\.work" && echo hit || true)"

if [ "$has_boot" -gt 0 ] && [ "$has_repl" -gt 0 ] && { [ -n "$jobs_hit" ] || [ -n "$alarms_hit" ] || [ -n "$wm_hit" ]; }; then
  echo "LNS EE-2 RESULT=PASS"
  exit 0
else
  echo "LNS EE-2 RESULT=FAIL"
  exit 1
fi
