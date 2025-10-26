#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/_repo/scheduled_workers.txt"
mkdir -p "$(dirname "$OUT")"

{
  echo "SCHEDULED_WORKERS v6.0"
  echo

  adb get-state >/dev/null 2>&1 || { echo "no device"; exit 2; }
  adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "app not installed"; exit 3; }

  echo "-- JobScheduler --"
  adb shell dumpsys jobscheduler | grep -A4 "$PKG" || echo "<none>"
  echo

  echo "-- WorkManager (activity service) --"
  adb shell dumpsys activity service WorkManager | grep -A6 "$PKG" || echo "<none>"
  echo

  echo "-- WorkManager (internal DB) --"
  adb exec-out run-as "$PKG" sh -c '
    db="databases/androidx.work.workdb"
    [ -f "$db" ] || { echo "<no workdb>"; exit 0; }
    sqlite3 "$db" "SELECT id,worker_class_name,state,schedule_requested_at,run_attempt_count FROM workspec;" 2>/dev/null || true
  ' 2>/dev/null || echo "<no access>"

  echo
  echo "DONE"
} | tee "$OUT"
