#!/bin/bash
set -euo pipefail

PKG="com.nick.myrecoverytracker"
DATA_DIR="/sdcard/Android/data/${PKG}/files"
MAX_HOURS=6
FAIL=0

fail() { echo "FAIL: $*"; FAIL=1; }
ok()   { echo "OK  : $*"; }

now_ts=$(date +%s)

check_fresh() {
  local file="$1" label="$2" ts age
  ts=$(adb shell stat -c %Y "${DATA_DIR}/${file}" 2>/dev/null || true)
  [ -z "$ts" ] && fail "$label not visible via adb shell (${file})" && return
  age=$(( (now_ts - ts) / 3600 ))
  [ "$age" -le "$MAX_HOURS" ] && ok "$label fresh (${age}h)" || fail "$label stale (${age}h)"
}

check_fresh "heartbeat.csv" "Heartbeat"
check_fresh "daily_summary.csv" "Daily summary"
check_fresh "unlock_log.csv" "Unlock log"
check_fresh "screen_log.csv" "Screen log"
check_fresh "notification_log.csv" "Notification log"
check_fresh "daily_app_usage_minutes.csv" "App usage minutes"
check_fresh "daily_usage_entropy.csv" "Usage entropy"

pid=$(adb shell pidof "$PKG" 2>/dev/null || true)
[ -n "$pid" ] && ok "Process running (pid=$pid)" || fail "Process not running"

exit "$FAIL"
