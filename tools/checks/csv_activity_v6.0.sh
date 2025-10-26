#!/bin/bash
set -euo pipefail

PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/_repo/csv_activity.txt"
mkdir -p "$(dirname "$OUT")"
: > "$OUT"

log(){ echo "$@" | tee -a "$OUT"; }

FILES="
files/daily_unlocks.csv
files/daily_sleep_summary.csv
files/daily_sleep_duration.csv
files/daily_sleep_time.csv
files/daily_wake_time.csv
files/daily_late_night_screen_usage.csv
files/daily_movement_intensity.csv
files/daily_movement.csv
files/daily_distance_log.csv
files/daily_notification_engagement.csv
files/daily_notification_latency.csv
files/daily_app_switching.csv
files/daily_app_usage_minutes.csv
files/daily_app_usage_minutes_by_app.csv
files/daily_app_usage_other.csv
files/daily_app_starts_by_package.csv
files/daily_usage_events.csv
files/daily_usage_entropy.csv
files/app_category_daily.csv
files/unlock_log.csv
files/screen_log.csv
files/usage_events.csv
files/location_log.csv
files/movement_log.csv
files/movement_windows.csv
files/notification_log.csv
files/notification_events.csv
files/notification_latency_log.csv
files/heartbeat.csv
files/unlock_diag.csv
files/notifications.csv
files/redcap_queue.csv
files/app_switches.csv
"

if ! adb get-state >/dev/null 2>&1; then log "no device"; exit 2; fi
if ! adb shell pm path "$PKG" >/dev/null 2>&1; then log "app not installed"; exit 3; fi

TODAY="$(adb shell 'toybox date +%F 2>/dev/null || date +%F' 2>/dev/null | tr -d $'\r')"

log "CSV_ACTIVITY v6.0"
log "today=$TODAY"
log ""

while IFS= read -r f; do
  [ -n "$f" ] || continue
  if ! adb exec-out run-as "$PKG" sh -c '[ -s "'"$f"'" ]' >/dev/null 2>&1; then
    log "$f: MISSING"
    continue
  fi
  COUNT="$(adb exec-out run-as "$PKG" sh -c '
    d="'"$TODAY"'"; f="'"$f"'"
    n=$(grep -a -c "^$d" "$f" 2>/dev/null || true)
    if [ "${n:-0}" -eq 0 ]; then n=$(grep -a -c "$d" "$f" 2>/dev/null || true); fi
    echo "${n:-0}"
  ' | tr -d $'\r' | awk "{print \$1+0}")"
  if [ "${COUNT:-0}" -gt 0 ]; then
    log "$f: WRITING (rows_today=$COUNT)"
  else
    log "$f: IDLE (rows_today=0)"
  fi
done <<< "$FILES"
