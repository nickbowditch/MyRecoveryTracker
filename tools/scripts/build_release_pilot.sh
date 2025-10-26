#!/bin/sh
set -eu

EXPECTED_CODE=10000
EXPECTED_NAME=1.0.0-pilot
META="app/build/outputs/apk/release/output-metadata.json"

./gradlew :app:clean :app:assembleRelease

code=$(sed -n 's/.*"versionCode": *\([0-9][0-9]*\).*/\1/p' "$META")
name=$(sed -n 's/.*"versionName": *"\([^"]*\)".*/\1/p' "$META")

[ "$code" = "$EXPECTED_CODE" ] || { echo "❌ versionCode $code != $EXPECTED_CODE"; exit 2; }
[ "$name" = "$EXPECTED_NAME" ] || { echo "❌ versionName $name != $EXPECTED_NAME"; exit 3; }

echo "✅ Pilot version locked: $name ($code)"
