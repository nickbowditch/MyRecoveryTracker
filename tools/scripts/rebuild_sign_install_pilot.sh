#!/bin/sh
set -eu

PKG="com.nick.myrecoverytracker"
APKDIR="app/build/outputs/apk/release"
UNSIGNED="$APKDIR/app-release-unsigned.apk"
SIGNED="$APKDIR/app-release-signed.apk"
OUTDIR="evidence/v6.0/builds"
META="$APKDIR/output-metadata.json"
mkdir -p "$OUTDIR" "$APKDIR"

# 1) Build release (skip lint so it can't block)
./gradlew :app:clean :app:assembleRelease \
  -x lintVitalRelease -x lintVitalAnalyzeRelease -x lintReportRelease -x lintRelease

# 2) Ensure unsigned exists
[ -f "$UNSIGNED" ] || { echo "❌ Missing $UNSIGNED"; exit 2; }

# 3) Print baked-in version from output-metadata.json
VC=""; VN=""
if command -v jq >/dev/null 2>&1; then
  VC="$(jq -r '.elements[0].versionCode' "$META" 2>/dev/null || echo "")"
  VN="$(jq -r '.elements[0].versionName' "$META" 2>/dev/null || echo "")"
else
  VC="$(awk -F: '/"versionCode"/{gsub(/[^0-9]/,"",$2); print $2; exit}' "$META" || true)"
  VN="$(awk -F\" '/"versionName"/{print $4; exit}' "$META" || true)"
fi
echo "Built: versionCode=${VC:-?} versionName=${VN:-?}" | tee "$OUTDIR/build_version.txt"

# 4) Find apksigner
APS="$(command -v apksigner || true)"
if [ -z "$APS" ]; then
  SDK_ROOT="${ANDROID_SDK_ROOT:-${ANDROID_HOME:-$HOME/Library/Android/sdk}}"
  APS="$(find "$SDK_ROOT"/build-tools -type f -name apksigner 2>/dev/null | sort -V | tail -n1 || true)"
fi
[ -x "${APS:-}" ] || { echo "❌ apksigner not found (install Android Build-Tools)"; exit 3; }

# 5) Sign with debug keystore
KS="$HOME/.android/debug.keystore"
[ -f "$KS" ] || { echo "❌ Missing debug keystore at $KS"; exit 4; }
"$APS" sign --ks "$KS" --ks-pass pass:android --key-pass pass:android \
  --ks-key-alias androiddebugkey --out "$SIGNED" "$UNSIGNED"
"$APS" verify -v "$SIGNED" >/dev/null

# 6) Install and verify on device
adb get-state >/dev/null 2>&1
adb uninstall "$PKG" >/dev/null 2>&1 || true
adb install -r "$SIGNED"
adb shell dumpsys package "$PKG" | grep -E 'version(Code|Name)=' | tee "$OUTDIR/device_version.txt"
