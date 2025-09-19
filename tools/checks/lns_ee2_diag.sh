#!/usr/bin/env bash
PKG="com.nick.myrecoverytracker"
echo "[receivers]"
adb shell dumpsys package "$PKG" 2>/dev/null | sed -n '/Receivers:/,/Services:/p'
echo "[jobscheduler]"
adb shell dumpsys jobscheduler 2>/dev/null | sed -n "1,2000p" | grep -i -n -A2 -B2 "$PKG" || true
echo "[alarms]"
adb shell dumpsys alarm 2>/dev/null | grep -i -n -A2 -B2 "$PKG" || true
echo "[services]"
adb shell dumpsys activity services 2>/dev/null | grep -i -n -A3 -B3 "$PKG\|workmanager\|androidx\.work" || true
