#!/usr/bin/env bash
set -euo pipefail

APP_ID="com.nick.myrecoverytracker"
INGEST_ROOT="$HOME/Documents/PHD/INGEST/raw"

# Participant / device ID – default to your Pixel test ID
PID="${1:-P_TEST_PIXEL12PRO}"
DEST="${INGEST_ROOT}/${PID}"

echo "Ingest destination: ${DEST}"
mkdir -p "${DEST}"

CSV_LIST=(
  "heartbeat.csv"
  "daily_summary.csv"
  "unlock_diag.csv"
  "unlock_log.csv"
  "screen_log.csv"
  "usage_events.csv"
  "daily_usage_entropy.csv"
  "daily_app_usage_minutes.csv"
  "daily_app_starts_by_package.csv"
  "distance_today.csv"
  "daily_distance_log.csv"
  "daily_sleep_summary.csv"
  "daily_sleep_duration.csv"
  "daily_sleep_quality.csv"
  "daily_sleep_summary_legacy.csv"
  "daily_sleep_time.csv"
  "daily_wake_time.csv"
  "daily_notification_engagement.csv"
  "notification_log.csv"
  "notification_heartbeat.csv"
  "daily_late_night_screen_usage.csv"
  "app_category_daily.csv"
  "log_export.csv"
  "log_retention.csv"
  "redcap_receipts.csv"
)

for f in "${CSV_LIST[@]}"; do
  echo "Copying ${f}"
  if adb shell run-as "${APP_ID}" test -f "files/${f}" 2>/dev/null; then
    adb shell run-as "${APP_ID}" cat "files/${f}" > "${DEST}/${f}"
  else
    echo "  (missing on device: ${f})"
  fi
done

echo "Done. Files for ${PID} now in ${DEST}"
