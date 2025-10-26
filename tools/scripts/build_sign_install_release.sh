#!/bin/sh
set -eu

PKG="com.nick.myrecoverytracker"
OUTDIR="evidence/v6.0/builds"
APKDIR="app/build/outputs/apk/release"
mkdir -p "$OUTDIR"

./gradlew :app:clean :app:assembleRelease

[ -d "$APKDIR" ] || { echo "❌ no dir $APKDIR"; exit 2; }

UNSIGNED="$(ls -1 "$APKDIR"/*-unsigned.apk 2>/dev/null | head -n1 || true)"
SIGNED_DEFAULT="$(ls -1 "$APKDIR"/*.apk 2>/dev/null | grep -v -- '-unsigned\.apk$' | head -n1 || true)"

# pick apksigner
SDK="${ANDROID_SDK_ROOT:-${ANDROID_HOME:-}}"
if [ -n "$SDK" ]; then
  APS="$(ls -1 "$SDK"/build-tools/*/apksigner 2>/dev/null | sort -V | tail -n1 || true)"
else
  APS="$(command -v apksigner || true)"
fi
[ -n "$APS" ] || { echo "❌ apksigner not found (set ANDROID_SDK_ROOT)"; exit 3; }

# sign if needed (using debug keystore)
if [ -n "$UNSIGNED" ]; then
  KS="$HOME/.android/debug.keystore"
  [ -f "$KS" ] || { echo "❌ debug.keystore not found at $KS"; exit 4; }
  OUTAPK="$APKDIR/app-release-signed.apk"
  "$APS" sign --ks "$KS" --ks-pass pass:android --key-pass pass:android --ks-key-alias androiddebugkey --out "$OUTAPK" "$UNSIGNED"
  "$APS" verify -v "$OUTAPK" >/dev/null
  APK="$OUTAPK"
else
  [ -n "$SIGNED_DEFAULT" ] || { echo "❌ no APKs in $APKDIR"; exit 5; }
  APK="$SIGNED_DEFAULT"
fi

adb get-state >/dev/null 2>&1
adb uninstall "$PKG" >/dev/null 2>&1 || true
adb install -r "$APK"

adb shell dumpsys package "$PKG" | grep -E 'version(Code|Name)=' | tee "$OUTDIR/device_version.txt"
