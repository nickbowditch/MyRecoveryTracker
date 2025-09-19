#!/bin/bash
PKG=com.nick.myrecoverytracker
TODAY=$(date +%F)
YESTERDAY=$(date -v -1d +%F)

printf "%-30s %-12s %-12s %-12s\n" "Feature" "Yesterday" "RolledUp" "Today"
printf "%-30s %-12s %-12s %-12s\n" "-------" "---------" "--------" "-----"

check_daily() {
  local file=$1; local label=$2
  local y t
  y=$(adb exec-out run-as "$PKG" sh -c "grep -c '^$YESTERDAY' files/$file 2>/dev/null" || echo 0)
  t=$(adb exec-out run-as "$PKG" sh -c "grep -c '^$TODAY' files/$file 2>/dev/null" || echo 0)
  local rolled="NO"
  if [ "${y:-0}" -gt 0 ] && [ "${t:-0}" -gt 0 ]; then rolled="YES"; fi
  printf "%-30s %-12s %-12s %-12s\n" "$label" "${y:-0}" "$rolled" "${t:-0}"
}

check_log() {
  local file=$1; local label=$2
  local y t
  y=$(adb exec-out run-as "$PKG" sh -c "grep '^$YESTERDAY' files/$file 2>/dev/null | wc -l" || echo 0)
  t=$(adb exec-out run-as "$PKG" sh -c "grep '^$TODAY' files/$file 2>/dev/null | wc -l" || echo 0)
  printf "%-30s %-12s %-12s %-12s\n" "$label" "${y:-0}" "-" "${t:-0}"
}

# Daily rollups
check_daily daily_sleep.csv                      "Sleep daily"
check_daily daily_late_night_screen_usage.csv   "Late night screen daily"
check_daily daily_light_exposure.csv            "Light exposure daily"
check_daily daily_distance_log.csv              "Distance daily"
check_daily daily_movement_intensity.csv        "Movement intensity daily"
check_daily daily_app_usage_minutes.csv         "App usage by category daily"
check_daily daily_usage_events.csv              "Usage events daily"
check_daily daily_app_switching.csv             "App switching daily"
check_daily daily_notification_engagement.csv   "Notification engagement"
check_daily daily_notification_latency.csv      "Notification latency"

# Raw logs
check_log   unlock_log.csv                      "Unlock log"
check_log   screen_log.csv                      "Screen log"
check_log   ambient_lux.csv                     "Ambient lux"
check_log   usage_events.csv                    "Usage events (raw)"
check_log   notification_log.csv                "Notification log (raw)"
check_log   redcap_upload_log.csv               "REDCap upload log"
