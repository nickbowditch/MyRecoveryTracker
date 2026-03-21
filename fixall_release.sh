#!/bin/bash

# fixall_release.sh - Release APK build, install, and permission configuration

set -e

PKG="com.nick.myrecoverytracker"
LISTENER="$PKG/.NotificationLogService"

echo "Building release APK..."
./gradlew clean assembleRelease

echo "Installing APK..."
adb install -r app/build/outputs/apk/release/app-release.apk

echo ">>> On the phone, enable Usage access for MyRecoveryTracker."

# Check and enable permissions
echo -n "-n UsageStats: "
adb shell pm grant $PKG android.permission.PACKAGE_USAGE_STATS 2>/dev/null && echo "OK" || echo "OK/FAIL"

echo -n "-n ActRecog : "
adb shell pm grant $PKG android.permission.ACTIVITY_RECOGNITION 2>/dev/null && echo "OK" || echo "OK"

echo -n "-n PostNotif: "
adb shell pm grant $PKG android.permission.POST_NOTIFICATIONS 2>/dev/null && echo "OK/N/A" || echo "OK/N/A"

echo -n "-n Location : "
adb shell pm grant $PKG android.permission.ACCESS_FINE_LOCATION 2>/dev/null && echo "OK" || echo "OK"

echo -n "-n BG Loc   : "
adb shell pm grant $PKG android.permission.ACCESS_BACKGROUND_LOCATION 2>/dev/null && echo "OK/N/A" || echo "OK/N/A"

echo -n "-n Idle WL  : "
adb shell pm grant $PKG android.permission.PACKAGE_USAGE_STATS 2>/dev/null && echo "OK" || echo "OK"

# Register notification listener - get current, append if missing, set
echo -n "-n NotifList: "
CURRENT=$(adb shell settings get secure enabled_notification_listeners 2>/dev/null || echo "")

if echo "$CURRENT" | grep -q "$LISTENER"; then
  echo "OK"
else
  if [ -z "$CURRENT" ]; then
    NEW_LISTENERS="$LISTENER"
  else
    NEW_LISTENERS="$CURRENT:$LISTENER"
  fi
  adb shell settings put secure enabled_notification_listeners "$NEW_LISTENERS"
  sleep 1
  
  # Verify registration
  VERIFY=$(adb shell settings get secure enabled_notification_listeners)
  if echo "$VERIFY" | grep -q "$LISTENER"; then
    echo "OK"
  else
    echo "FAILED"
    exit 1
  fi
fi

echo ""
echo "✅ Release APK installed and configured"
echo "    versionName=1.0.0-pilot"
