#!/bin/zsh
PKG="com.nick.myrecoverytracker"
S=0

declared=$(adb shell dumpsys package $PKG | grep android.permission.PACKAGE_USAGE_STATS || true)
if [ -z "$declared" ]; then
  echo "EE-1 FAIL: PACKAGE_USAGE_STATS not declared"; S=1
else
  if adb shell appops get $PKG PACKAGE_USAGE_STATS 2>/dev/null | grep -E "allow|foreground" >/dev/null; then
    echo "EE-1 PASS: usage stats permission granted"
  else
    echo "EE-1 FAIL: usage stats permission denied"; S=1
  fi
fi

declared_act=$(adb shell dumpsys package $PKG | grep android.permission.ACTIVITY_RECOGNITION || true)
if [ -n "$declared_act" ]; then
  if adb shell dumpsys package $PKG | grep ACTIVITY_RECOGNITION | grep -q granted=true; then
    echo "EE-1 PASS: activity recognition granted"
  else
    echo "EE-1 FAIL: activity recognition denied"; S=1
  fi
fi

exit $S
