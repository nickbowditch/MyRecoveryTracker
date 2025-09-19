#!/usr/bin/env bash
PKG="com.nick.myrecoverytracker"
echo "[candidate actions]"
adb shell dumpsys package "$PKG" 2>/dev/null | awk '/Receivers:/{r=1;next} r&&/action:/{print}'
echo "[file head]"
adb exec-out run-as "$PKG" sh -c 'head -n5 files/daily_late_screen.csv 2>/dev/null' | tr -d '\r'
echo "[doze state]"
adb shell dumpsys deviceidle 2>/dev/null | sed -n '1,120p'
