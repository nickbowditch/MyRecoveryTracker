#!/usr/bin/env bash
set -euo pipefail
PKG="com.nick.myrecoverytracker"
mkdir -p tools/checks/_artifacts
./gradlew :app:qaMetricsCheck --no-daemon --console=plain > tools/checks/_artifacts/qaMetricsCheck.out 2>&1 || true
DEV_HDR="$(adb exec-out run-as "$PKG" sh -c 'head -n1 files/daily_metrics.csv 2>/dev/null' | tr -d '\r')"
LOCK_HDR="$(head -n1 locks/daily_metrics.header 2>/dev/null || true)"
echo "$DEV_HDR" > tools/checks/_artifacts/device_daily_metrics.header
echo "$LOCK_HDR" > tools/checks/_artifacts/locked_daily_metrics.header
printf "device_header: %s\n" "$DEV_HDR"
printf "locked_header: %s\n" "$LOCK_HDR"
printf "qaMetricsCheck.out saved to tools/checks/_artifacts/qaMetricsCheck.out\n"
