#!/bin/sh
set -eu

PKG="com.nick.myrecoverytracker"
LISTENER="$PKG/.NotificationLogService"

./gradlew clean assembleRelease

adb uninstall "$PKG" 2>/dev/null || true
adb install app/build/outputs/apk/release/app-release.apk

adb shell appops set --user 0 "$PKG" GET_USAGE_STATS allow
adb shell pm grant "$PKG" android.permission.ACTIVITY_RECOGNITION 2>/dev/null || true
adb shell pm grant "$PKG" android.permission.POST_NOTIFICATIONS 2>/dev/null || true
adb shell pm grant "$PKG" android.permission.ACCESS_COARSE_LOCATION 2>/dev/null || true
adb shell pm grant "$PKG" android.permission.ACCESS_FINE_LOCATION 2>/dev/null || true
adb shell pm grant "$PKG" android.permission.ACCESS_BACKGROUND_LOCATION 2>/dev/null || true
adb shell cmd deviceidle whitelist +"$PKG" >/dev/null 2>&1

CURRENT=$(adb shell settings get secure enabled_notification_listeners 2>/dev/null || echo "")
if ! echo "$CURRENT" | grep -q "$PKG"; then
  if [ -z "$CURRENT" ]; then
    NEW_LISTENERS="$LISTENER"
  else
    NEW_LISTENERS="$CURRENT:$LISTENER"
  fi
  adb shell settings put secure enabled_notification_listeners "$NEW_LISTENERS"
fi

adb shell am force-stop "$PKG"
adb shell monkey -p "$PKG" -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1
sleep 1

echo ">>> On the phone, enable Usage access for MyRecoveryTracker."
adb shell am start -a android.settings.USAGE_ACCESS_SETTINGS >/dev/null 2>&1 || true

adb shell am broadcast -n "$PKG/.TriggerReceiver" -a "$PKG.ACTION_RUN_REDCAP_UPLOAD" >/dev/null 2>&1 || true

echo -n "UsageStats: "; adb shell appops get "$PKG" GET_USAGE_STATS | grep -q "allow" && echo OK || echo MISSING
echo -n "ActRecog : "; adb shell dumpsys package "$PKG" | grep -A1 ACTIVITY_RECOGNITION | grep -q granted=true && echo OK || echo MISSING
echo -n "PostNotif: "; adb shell dumpsys package "$PKG" | grep -A1 POST_NOTIFICATIONS | grep -q granted=true && echo OK/N/A || echo MISSING/N/A
echo -n "Location : "; adb shell dumpsys package "$PKG" | egrep -A1 'ACCESS_(COARSE|FINE)_LOCATION' | grep -q granted=true && echo OK || echo MISSING
echo -n "BG Loc   : "; adb shell dumpsys package "$PKG" | grep -A1 ACCESS_BACKGROUND_LOCATION | grep -q granted=true && echo OK/N/A || echo MISSING/N/A
echo -n "Idle WL  : "; adb shell dumpsys deviceidle | awk -v p="$PKG" '
  /Whitelist user apps:/ {s=1; next}
  s && NF==0 {s=0}
  s && index($0,p){f=1}
  END{print (f?"OK":"MISSING")}'
echo -n "NotifList: "; adb shell settings get secure enabled_notification_listeners | grep -q "$PKG" && echo OK || echo MISSING

echo ""
echo "✅ Release APK installed and configured"
adb shell dumpsys package "$PKG" | grep versionName | head -1
