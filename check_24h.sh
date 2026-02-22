#!/bin/sh
set -eu

PKG="com.nick.myrecoverytracker"

echo "=== DEVICE DATE/TIME ==="
adb shell date || true

echo
echo "=== files dir (sizes + mtimes) ==="
adb shell run-as "$PKG" ls -l files || { echo "files/ missing"; exit 1; }

echo
echo "=== daily_summary.csv (last 5) ==="
adb shell run-as "$PKG" tail -5 files/daily_summary.csv || echo "missing"

echo
echo "=== daily_usage_entropy.csv (last 5) ==="
adb shell run-as "$PKG" tail -5 files/daily_usage_entropy.csv || echo "missing"

echo
echo "=== daily_app_usage_minutes.csv (last 5) ==="
adb shell run-as "$PKG" tail -5 files/daily_app_usage_minutes.csv || echo "missing"

echo
echo "=== daily_app_starts_by_package.csv (last 5) ==="
adb shell run-as "$PKG" tail -5 files/daily_app_starts_by_package.csv || echo "missing"

echo
echo "=== app_category_daily.csv (last 5) ==="
adb shell run-as "$PKG" tail -5 files/app_category_daily.csv || echo "missing"

echo
echo "=== notification_log.csv (last 5) ==="
adb shell run-as "$PKG" tail -5 files/notification_log.csv || echo "missing"

echo
echo "=== notification_heartbeat.csv (last 5) ==="
adb shell run-as "$PKG" tail -5 files/notification_heartbeat.csv || echo "missing"

echo
echo "=== daily_late_night_screen_usage.csv ==="
adb shell run-as "$PKG" cat files/daily_late_night_screen_usage.csv || echo "missing"

echo
echo "=== distance_today.csv (last 5) ==="
adb shell run-as "$PKG" tail -5 files/distance_today.csv || echo "missing"

echo
echo "=== daily_distance_log.csv (last 5) ==="
adb shell run-as "$PKG" tail -5 files/daily_distance_log.csv || echo "missing"

echo
echo "=== location_log.csv (last 5) ==="
adb shell run-as "$PKG" tail -5 files/location_log.csv || echo "missing"

echo
echo "=== location_log_raw.csv (last 5) ==="
adb shell run-as "$PKG" tail -5 files/location_log_raw.csv || echo "missing"

echo
echo "=== sleep: daily_sleep_duration.csv (last 5) ==="
adb shell run-as "$PKG" tail -5 files/daily_sleep_duration.csv || echo "missing"

echo
echo "=== sleep: daily_sleep_quality.csv (last 5) ==="
adb shell run-as "$PKG" tail -5 files/daily_sleep_quality.csv || echo "missing"

echo
echo "=== sleep: daily_sleep_summary.csv (last 5) ==="
adb shell run-as "$PKG" tail -5 files/daily_sleep_summary.csv || echo "missing"

echo
echo "=== sleep: daily_sleep_summary_legacy.csv (last 5) ==="
adb shell run-as "$PKG" tail -5 files/daily_sleep_summary_legacy.csv || echo "missing"

echo
echo "=== sleep: daily_sleep_time.csv (last 5) ==="
adb shell run-as "$PKG" tail -5 files/daily_sleep_time.csv || echo "missing"

echo
echo "=== sleep: daily_wake_time.csv (last 5) ==="
adb shell run-as "$PKG" tail -5 files/daily_wake_time.csv || echo "missing"

echo
echo "=== heartbeat.csv (last 10) ==="
adb shell run-as "$PKG" tail -10 files/heartbeat.csv || echo "missing"

echo
echo "=== daily_metrics_upload.csv ==="
adb shell run-as "$PKG" cat files/daily_metrics_upload.csv || echo "missing"

echo
echo "=== redcap_receipts.csv (last 10) ==="
adb shell run-as "$PKG" tail -10 files/redcap_receipts.csv || echo "missing"

echo
echo "=== log_export.csv ==="
adb shell run-as "$PKG" cat files/log_export.csv || echo "missing"

echo
echo "=== log_retention.csv ==="
adb shell run-as "$PKG" cat files/log_retention.csv || echo "missing"
