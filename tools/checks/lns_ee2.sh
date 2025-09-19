#!/usr/bin/env bash
PKG="com.nick.myrecoverytracker"

has_boot="$(adb shell dumpsys package "$PKG" 2>/dev/null | awk '/Receivers:/{r=1} r&&/BOOT_COMPLETED/{print; exit}' | wc -l | tr -d '[:space:]')"
has_repl="$(adb shell dumpsys package "$PKG" 2>/dev/null | awk '/Receivers:/{r=1} r&&/PACKAGE_REPLACED/{print; exit}' | wc -l | tr -d '[:space:]')"

adb shell am broadcast -a android.intent.action.BOOT_COMPLETED -p "$PKG" >/dev/null 2>&1
sleep 1
adb shell am broadcast -a android.intent.action.PACKAGE_REPLACED --es "android.intent.extra.PACKAGES" "$PKG" >/dev/null 2>&1
sleep 2

jobs="$(adb shell dumpsys jobscheduler 2>/dev/null | awk -v p="$PKG" 'tolower($0) ~ tolower(p){f=1} END{print (f?"hit":"")}')"
wm_svc="$(adb shell dumpsys activity services 2>/dev/null | awk 'tolower($0) ~ /workmanager|androidx\.work/ {f=1} END{print (f?"hit":"")}')"

if [ "$has_boot" -gt 0 ] && [ "$has_repl" -gt 0 ] && { [ -n "$jobs" ] || [ -n "$wm_svc" ]; }; then
  echo "LNS EE-2 RESULT=PASS"
  exit 0
else
  echo "LNS EE-2 RESULT=FAIL"
  exit 1
fi
