#!/usr/bin/env bash
set -euo pipefail

PKG="com.nick.myrecoverytracker"
RUN_AS="adb shell run-as ${PKG}"
FILES_DIR="files"

echo "=== MyRecoveryTracker health check ==="

echo
echo "[1] App process status"
app_pid="$(adb shell pidof ${PKG} 2>/dev/null | tr -d '\r' || true)"
if [[ -n "${app_pid}" ]]; then
  echo "  APP: RUNNING (pid ${app_pid})"
else
  echo "  APP: NOT RUNNING"
fi

echo
echo "[2] daily_summary.csv (today present?)"
TODAY="$(date +%Y-%m-%d)"
if ${RUN_AS} test -f "${FILES_DIR}/daily_summary.csv" 2>/dev/null; then
  echo "  File exists: ${FILES_DIR}/daily_summary.csv"
  echo "  Last 10 lines:"
  ${RUN_AS} tail -n 10 "${FILES_DIR}/daily_summary.csv" || true
  echo
  if ${RUN_AS} grep -q "${TODAY}" "${FILES_DIR}/daily_summary.csv" 2>/dev/null; then
    echo "  TODAY CHECK: OK (found ${TODAY})"
  else
    echo "  TODAY CHECK: MISSING (no ${TODAY} row)"
  fi
else
  echo "  File missing: ${FILES_DIR}/daily_summary.csv"
fi

echo
echo "[3] Key CSV tails (last 10 lines each)"
CSV_LIST=(
  "heartbeat.csv"
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
  "daily_notification_engagement.csv"
)

for f in "${CSV_LIST[@]}"; do
  echo
  echo "---- ${FILES_DIR}/${f} ----"
  if ${RUN_AS} test -f "${FILES_DIR}/${f}" 2>/dev/null; then
    ${RUN_AS} tail -n 10 "${FILES_DIR}/${f}" || true
  else
    echo "  (missing)"
  fi
done

echo
echo "=== Health check finished ==="
