#!/bin/sh
set -eu

PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/movement/daily_movement_intensity_v6_0.txt"
CSV="daily_movement_intensity.csv"
WORKER="MovementIntensityDailyWorker"

mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "RESULT=FAIL - no device"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "RESULT=FAIL - app not installed"; exit 3; }

echo "STATUS:"
rows="$(adb exec-out run-as "$PKG" sh -c "wc -l < files/$CSV 2>/dev/null" | tr -d '\r')"
[ "${rows:-0}" -gt 1 ] || { echo "rows_today=0"; echo "RESULT=FAIL - no data rows"; exit 1; }
echo "rows_total=$rows"

echo
echo "-- DEBUG: header & tail ---"
adb exec-out run-as "$PKG" sh -c "head -n 1 files/$CSV; tail -n 3 files/$CSV" | tr -d '\r'

today="$(date +%Y-%m-%d)"
yest="$(date -d 'yesterday' +%Y-%m-%d 2>/dev/null || date -v-1d +%Y-%m-%d)"
lines_today="$(adb exec-out run-as "$PKG" grep -c "$today" files/$CSV 2>/dev/null || true)"
lines_yest="$(adb exec-out run-as "$PKG" grep -c "$yest" files/$CSV 2>/dev/null || true)"
echo
echo "LAST WRITTEN:"
echo "today_rows=$lines_today"
echo "yesterday_rows=$lines_yest"
[ "$lines_today" -gt 0 ] || { echo "RESULT=FAIL - no rows for today"; exit 1; }

echo
echo "WORKER(S):"
adb exec-out run-as "$PKG" grep "$WORKER" /data/user/0/$PKG/no_backup/androidx.work.workdb 2>/dev/null | head -n 5 | tr -d '\r' || true
jobstate="$(adb shell cmd jobscheduler get-job-state "$PKG" 2>/dev/null | grep -i "$WORKER" || true)"
echo "$jobstate" | grep -q "$WORKER" && echo "JobScheduler: active" || echo "JobScheduler: not found"

echo
echo "REDCap Mapping:"
if adb exec-out run-as "$PKG" grep -q "movement_intensity" shared_prefs/redcap_prefs.xml 2>/dev/null; then
  echo "redcap_map: present"
else
  echo "redcap_map: not found"
fi

echo
echo "REDCap Status:"
if [ -n "${REDCAP_API_URL:-}" ] && [ -n "${REDCAP_API_TOKEN:-}" ]; then
  echo "redcap_status: env present"
else
  echo "redcap_status: skipped (no env)"
fi

echo
echo "NOTES:"
echo "sanity check: plausible row count & fresh writes confirmed"
echo "RESULT=PASS"
exit 0
