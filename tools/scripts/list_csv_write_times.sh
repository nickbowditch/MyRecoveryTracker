#!/bin/sh
set -e
PKG=com.nick.myrecoverytracker
EXT="/sdcard/Android/data/$PKG/files"
OUT="evidence/csv_write_times_$(date -u +%Y%m%dT%H%M%SZ).txt"

cat > /tmp/expected_csvs.txt <<'EOF'
daily_unlocks.csv
unlock_log.csv
daily_sleep.csv
daily_sleep_summary.csv
daily_sleep_duration.csv
screen_log.csv
daily_lnsu.csv
daily_lnslu.csv
daily_late_night_screen_usage.csv
daily_notification_engagement.csv
notification_log.csv
daily_notification_latency.csv
notification_latency_log.csv
usage_events.csv
daily_usage_events.csv
app_category_daily.csv
daily_app_switching.csv
location_log.csv
daily_distance_log.csv
daily_movement_intensity.csv
EOF

{
  echo "=== WRITE TIMES FOR EXPECTED CSVS ==="
  echo "Timestamp: $(date -u '+%Y-%m-%dT%H:%M:%SZ')"
  echo

  while read -r f; do
    adb shell "stat -c '%n %y' $EXT/$f 2>/dev/null || echo 'MISSING $EXT/$f'" || true
  done < /tmp/expected_csvs.txt
} > "$OUT"

echo "✅ Output written to $OUT"
cat "$OUT"
