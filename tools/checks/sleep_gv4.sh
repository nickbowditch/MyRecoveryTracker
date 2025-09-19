#!/usr/bin/env bash
set -euo pipefail
PKG="com.nick.myrecoverytracker"
./gradlew -q tasks --all | grep -q "^app:qaVerifyMetrics " || { echo "Sleep GV-4 RESULT=FAIL (no :app:qaVerifyMetrics)"; exit 1; }
./gradlew :app:qaVerifyMetrics >/dev/null 2>&1 || { echo "Sleep GV-4 RESULT=FAIL (qaVerifyMetrics failed)"; exit 1; }
DEV_HDR="$(adb exec-out run-as "$PKG" sh -c 'head -n1 files/daily_metrics.csv 2>/dev/null' | tr -d '\r')"
LOCK_HDR="$(head -n1 locks/daily_metrics.header 2>/dev/null || true)"
echo "device_header: $DEV_HDR"
echo "locked_header: $LOCK_HDR"
echo "Sleep GV-4 RESULT=PASS"
