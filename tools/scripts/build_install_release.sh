#!/bin/sh
set -eu

PKG="com.nick.myrecoverytracker"
APK="app/build/outputs/apk/release/app-release.apk"
OUTDIR="evidence/v6.0/builds"
mkdir -p "$OUTDIR"

./gradlew :app:clean :app:assembleRelease

[ -f "$APK" ] || { echo "❌ Missing $APK (release not built)"; exit 2; }

adb get-state >/dev/null 2>&1
adb uninstall "$PKG" >/dev/null 2>&1 || true
adb install -r "$APK"

adb shell dumpsys package "$PKG" | grep -E 'version(Code|Name)=' | tee "$OUTDIR/device_version.txt"
