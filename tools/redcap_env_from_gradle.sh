#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."

echo "=== .gradle/gradle.properties ==="
if [ -f .gradle/gradle.properties ]; then
  grep -E '^REDCAP_(URL|TOKEN|PROJECT_ID)=' .gradle/gradle.properties || echo "no REDCAP_* in .gradle/gradle.properties"
else
  echo ".gradle/gradle.properties missing"
fi

echo
echo "=== ./gradle.properties ==="
if [ -f gradle.properties ]; then
  grep -E '^REDCAP_(URL|TOKEN|PROJECT_ID)=' gradle.properties || echo "no REDCAP_* in ./gradle.properties"
else
  echo "gradle.properties missing"
fi

./gradlew :app:assembleDebug >/dev/null

BUILD_CONFIG="app/build/generated/source/buildConfig/debug/com/nick/myrecoverytracker/BuildConfig.java"

echo
echo "=== BuildConfig.java REDCAP_* constants ==="
if [ -f "$BUILD_CONFIG" ]; then
  grep -E 'REDCAP_(URL|TOKEN|BASE_URL|API_TOKEN|PROJECT_ID)' "$BUILD_CONFIG" || echo "no REDCAP_* constants found"
else
  echo "BuildConfig.java not found at $BUILD_CONFIG"
fi
