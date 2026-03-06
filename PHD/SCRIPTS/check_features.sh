# write a fresh script to a clean path and run it
cat > /tmp/mrt_check.sh <<'EOF'
#!/usr/bin/env bash
set -euo pipefail

PKG=com.nick.myrecoverytracker
TODAY=$(date +%F)
if date -v -1d +%F >/dev/null 2>&1; then
  YESTERDAY=$(date -v -1d +%F)
else
  YESTERDAY=$(date -d "yesterday" +%F)
fi

ME="/tmp/mrt_check.sh"
printf 'RUNNING: %s\nSHA256: ' "$ME"; shasum -a 256 "$ME" | awk '{print $1}'
echo

printf "%-30s %-12s %-12s %-12s\n" "Feature" "Yesterday" "RolledUp" "Today"
printf "%-30s %-12s %-12s %-12s\n" "-------" "---------" "--------" "-----"

check_daily() {
  local file=$1 label=$2
  local y t rolled
  y=$(adb exec-out run-as "$PKG" sh -c "grep -c '^$YESTERDAY' files/$file 2>/dev/null" || echo 0)
  t=$(adb exec-out run-as "$PKG" sh -c "grep -c '^$TODAY'    files/$file 2>/dev/null" || echo 0)
  rolled="NO"; [ "$y" -gt 0 ] && [ "$t" -gt 0 ] && rolled="YES"
  printf "%-30s %-12s %-12s %-12s\n" "$label" "$y" "$rolled" "$t"
}

check_log() {
  local file=$1 label=$2
  local y t
  y=$(adb exec-out run-as "$PKG" sh -c "grep '^$YESTERDAY' files/$file 2>/dev/null | wc -l" || echo 0)
  t=$(adb exec-out run-as "$PKG" sh -c "grep '^$TODAY'    files/$file 2>/dev/null | wc -l" || echo 0)
  printf "%-30s %-12s %-12s %-12s\n" "$label" "$y" "-" "$t"
}

# Sleep (4 separate files)
check_daily daily_sleep_summary.csv    "Sleep summary"
check_daily daily_sleep_time.csv       "Sleep times"
check_daily daily_sleep_duration.csv   "Sleep duration"
check_daily daily_sleep_quality.csv    "Sleep quality"

# Other daily rollups
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
check_log   unlock_log.csv           "Unlock log"
check_log   location_log.csv         "Location log"
check_log   screen_log.csv           "Screen log"
check_log   ambient_lux.csv          "Ambient lux"
check_log   usage_events.csv         "Usage events (raw)"
check_log   notification_log.csv     "Notification log (raw)"
check_log   redcap_upload_log.csv    "REDCap upload log"

echo
echo "-- latest sleep rows --"
adb exec-out run-as "$PKG" sh -c '
for f in daily_sleep_summary.csv daily_sleep_time.csv daily_sleep_duration.csv daily_sleep_quality.csv; do
  if [ -f "files/$f" ]; then
    echo "# $f"
    tail -n 2 "files/$f"
  else
    echo "# $f (missing)"
  fi
done
'

echo
echo "-- App usage by category (today: $TODAY) --"
adb exec-out run-as "$PKG" sh -c '
f="files/daily_app_usage_minutes.csv"
if [ -f "$f" ]; then
  awk -F, -v d="'"$TODAY"'" "\$1==d && NF>=3 {printf \"%7.1f  %s\n\", \$3+0, \$2}" "$f" | sort -nr
  awk -F, -v d="'"$TODAY"'" "(\$1==d && NF>=3){s+=\$3} END{printf \"TOTAL  %.1f minutes\n\", s}" "$f"
else
  echo "(no daily_app_usage_minutes.csv)"
fi
'

echo
echo "-- App usage by category (yesterday: $YESTERDAY) --"
adb exec-out run-as "$PKG" sh -c '
f="files/daily_app_usage_minutes.csv"
if [ -f "$f" ]; then
  awk -F, -v d="'"$YESTERDAY"'" "\$1==d && NF>=3 {printf \"%7.1f  %s\n\", \$3+0, \$2}" "$f" | sort -nr
  awk -F, -v d="'"$YESTERDAY"'" "(\$1==d && NF>=3){s+=\$3} END{printf \"TOTAL  %.1f minutes\n\", s}" "$f"
else
  echo "(no daily_app_usage_minutes.csv)"
fi
'
EOF
chmod +x /tmp/mrt_check.sh
bash /tmp/mrt_check.sh