#!/bin/bash

echo "Checking for remaining field name discrepancies..."
echo ""

# Check source CSV files for old field names
echo "=== SOURCE CSV FILES ==="
echo ""

echo "late_night_screen_usage_yn in daily_late_night_screen_usage.csv:"
grep -o "late_night_screen_usage_yn" app/src/main/java/com/nick/myrecoverytracker/LateNightScreenRollupWorker.kt || echo "NOT FOUND"
echo ""

echo "notifications_delivered in daily_notification_engagement.csv:"
grep -o "notifications_delivered" app/src/main/java/com/nick/myrecoverytracker/NotificationEngagementWorker.kt || echo "NOT FOUND"
echo ""

echo "notifications_opened in daily_notification_engagement.csv:"
grep -o "notifications_opened" app/src/main/java/com/nick/myrecoverytracker/NotificationEngagementWorker.kt || echo "NOT FOUND"
echo ""

echo "notif_latency_p50_s in daily_notification_latency.csv:"
grep -o "notif_latency_p50_s" app/src/main/java/com/nick/myrecoverytracker/NotificationLatencyWorker.kt || echo "NOT FOUND"
echo ""

echo "daily_usage_entropy_bits in daily_usage_entropy.csv:"
grep -o "daily_usage_entropy_bits" app/src/main/java/com/nick/myrecoverytracker/UsageEntropyDailyWorker.kt || echo "NOT FOUND"
echo ""

echo "distance_km in daily_distance_log.csv:"
grep -o "distance_km" app/src/main/java/com/nick/myrecoverytracker/DistanceSummaryWorker.kt || echo "NOT FOUND"
echo ""

echo "movement_intensity in daily_movement_intensity.csv:"
grep -o "movement_intensity" app/src/main/java/com/nick/myrecoverytracker/MovementIntensityWorker.kt || echo "NOT FOUND"
echo ""

echo "=== REDCAP UPLOAD WORKER ==="
echo ""

echo "Checking RedcapUploadWorker.kt for field mappings:"
grep -E "(late_night|notif_posted|notif_engaged|notif_latency_median_s|notif_latency_n|usage_entropy|app_switch_count|distance_m|move_intensity_score)" app/src/main/java/com/nick/myrecoverytracker/RedcapUploadWorker.kt | head -20
