#!/bin/bash
PKG="com.nick.myrecoverytracker"
LOCK_PATH="app/locks/daily_metrics.header"

DEV_HDR="$(adb exec-out run-as "$PKG" sh -c 'head -n1 files/daily_metrics.csv 2>/dev/null' | tr -d '\r')"
LOCK_HDR="$(head -n1 "$LOCK_PATH" 2>/dev/null || true)"

RES="PASS"
./gradlew :app:qaVerifyMetrics --no-daemon --console=plain >/dev/null 2>&1 || RES="FAIL"

echo "device_header: $DEV_HDR"
echo "locked_header: $LOCK_HDR"
echo "lock_path: $LOCK_PATH"
echo "Sleep GV-4 RESULT=$RES"
