#!/bin/sh
set -e
OUT="evidence/expected_csvs.txt"
cat > "$OUT" <<'EOF'
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
cat "$OUT"
