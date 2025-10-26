#!/bin/sh
set -eu

PKG="com.nick.myrecoverytracker"
CSV="files/daily_notification_engagement.csv"
EXPECTED_WORKER="NotificationEngagementWorker"

echo "[INFO] Notification Engagement Row Healthcheck v6.0"
echo "[INFO] PKG=$PKG"
echo "[INFO] CSV=$CSV"
echo "[INFO] EXPECTED_WORKER=$EXPECTED_WORKER"

adb get-state >/dev/null 2>&1 || { echo "RESULT=FAIL - no device"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "RESULT=FAIL - app not installed"; exit 3; }

TODAY="$(adb shell 'toybox date +%F 2>/dev/null || date +%F' | tr -d '\r')"
echo "[INFO] TODAY=$TODAY"

adb exec-out run-as "$PKG" sh -c "test -f '$CSV'" >/dev/null 2>&1 || { echo "RESULT=FAIL - CSV missing"; exit 1; }

HEADER="$(adb exec-out run-as "$PKG" head -n 1 "$CSV" | tr -d '\r')"
[ -n "$HEADER" ] || { echo "RESULT=FAIL - empty header"; exit 1; }
echo "--- DEBUG: header & tail ---"
echo "  $HEADER"
adb exec-out run-as "$PKG" tail -n 5 "$CSV" | tr -d '\r' | sed 's/^/  /'

BODY="$(adb exec-out run-as "$PKG" tail -n +2 "$CSV" | tr -d '\r' | sed '/^[[:space:]]*$/d')"
[ -n "$BODY" ] || { echo "RESULT=FAIL - no data rows"; exit 1; }

ROWS_TODAY="$(printf '%s\n' "$BODY" | awk -F',' -v d="$TODAY" 'NF>0 && $1==d {c++} END{print c+0}')"
echo "rows_today: $ROWS_TODAY"

LAST3="$(printf '%s\n' "$BODY" | awk -F',' 'NF>0{d=$1;if(!seen[d]++){print d}}' | tail -n 3)"
echo "last_3_days:"
if [ -n "$LAST3" ]; then
  printf '%s\n' "$LAST3" | while IFS= read -r d; do
    [ -z "$d" ] && continue
    line="$(printf '%s\n' "$BODY" | awk -F',' -v D="$d" 'NF>0 && $1==D {print $0; exit}')"
    printf "  - %s : %s\n" "$d" "${line:-<no row>}"
  done
else
  echo "  <no recent dates found>"
fi

JOB_SNIPPET="$(adb shell dumpsys jobscheduler 2>/dev/null | tr -d '\r' | awk -v pat="$EXPECTED_WORKER" '$0~pat{print; n=5; next} n>0{print; n--}')"
WORKER_OK="no"; [ -n "$JOB_SNIPPET" ] && WORKER_OK="yes"
echo "worker_expected: $EXPECTED_WORKER"
echo "worker_found: $WORKER_OK"
[ -n "$JOB_SNIPPET" ] && { echo "--- DEBUG: jobscheduler grep ---"; printf '%s\n' "$JOB_SNIPPET" | sed 's/^/  /'; }

if [ "$ROWS_TODAY" -gt 0 ] && [ "$WORKER_OK" = "yes" ]; then
  echo "RESULT=PASS"; exit 0
else
  echo "RESULT=FAIL"; exit 1
fi
