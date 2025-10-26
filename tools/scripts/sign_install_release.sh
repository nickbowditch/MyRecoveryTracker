#!/bin/sh
set -eu

PKG="com.nick.myrecoverytracker"
APK_UNSIGNED="app/build/outputs/apk/release/app-release-unsigned.apk"
APK_SIGNED="app/build/outputs/apk/release/app-release-signed.apk"

# find apksigner (PATH first, then SDK default on macOS)
APS="$(command -v apksigner || true)"
if [ -z "$APS" ]; then
  SDK_ROOT="${ANDROID_SDK_ROOT:-${ANDROID_HOME:-$HOME/Library/Android/sdk}}"
  APS="$(find "$SDK_ROOT/build-tools" -type f -name apksigner 2>/dev/null | sort -V | tail -n1 || true)"
fi
[ -x "${APS:-}" ] || { echo "❌ apksigner not found. Install Android Build-Tools."; exit 1; }

# inputs
[ -f "$APK_UNSIGNED" ] || { echo "❌ Missing unsigned APK: $APK_UNSIGNED"; exit 2; }
KS="$HOME/.android/debug.keystore"
[ -f "$KS" ] || { echo "❌ Missing debug keystore at $KS"; exit 3; }

# sign
"$APS" sign \
  --ks "$KS" \
  --ks-pass pass:android \
  --key-pass pass:android \
  --ks-key-alias androiddebugkey \
  --out "$APK_SIGNED" \
  "$APK_UNSIGNED"

# verify + install
"$APS" verify -v "$APK_SIGNED" >/dev/null
adb uninstall "$PKG" >/dev/null 2>&1 || true
adb install -r "$APK_SIGNED"
adb shell dumpsys package "$PKG" | grep -E 'version(Code|Name)='
