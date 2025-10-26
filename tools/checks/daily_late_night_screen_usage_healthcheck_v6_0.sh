#!/bin/sh
set -eu

PKG="com.nick.myrecoverytracker"
CSV="files/daily_late_night_screen_usage.csv"
EXPECTED_WORKER="ScreenOnDailyWorker"
OUT="evidence/v6.0/sleep/daily_late_night_screen_usage_healthcheck_v6_0.txt"

mkdir -p "$(dirname "$OUT")"

echo "[INFO] Daily Late-Night Screen Usage Healthcheck v6.0"
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

IDX_LINE="$(printf '%s\n' "$HEADER" | tr '[:upper:]' '[:lower:]' | awk -F',' '{
  for(i=1;i<=NF;i++){
    h=$i; gsub(/^[ \t]+|[ \t]+$/,"",h)
    if(h=="late_night_minutes"||h=="minutes_late_night"||h=="screen_minutes"||h=="screen_time_min") mi=i
  }
  printf "mi=%d\n", mi+0
}')"

MI="$(printf '%s\n' "$IDX_LINE" | awk -F'[ =]+' '{for(i=1;i<=NF;i++) if($i=="mi"){print $(i+1); exit}}')"
[ "${MI:-0}" -gt 0 ] || { echo "RESULT=FAIL - late_night_minutes column not found"; exit 1; }

ROWS_TODAY="$(printf '%s\n' "$BODY" | awk -F',' -v d="$TODAY" 'NF>0 && $1==d {c++} END{print c+0}')"
echo "rows_today: $ROWS_TODAY"
[ "$ROWS_TODAY" -gt 0 ] || { echo "RESULT=FAIL - no rows for today"; exit 1; }

TODAY_LINE="$(printf '%s\n' "$BODY" | awk -F',' -v d="$TODAY" 'NF>0 && $1==d {print; exit}')"
MIN_VAL="$(printf '%s\n' "$TODAY_LINE" | awk -F',' -v i="$MI" 'NF>=i{print $i}')"
echo "today_values: late_night_minutes=${MIN_VAL:-<none>} (expected range: 0..300)"

SANITY_OK="yes"
echo "$MIN_VAL" | grep -Eq '^[0-9]+([.][0-9]+)?$' || SANITY_OK="no"
[ "$SANITY_OK" = "yes" ] && awk -v m="$MIN_VAL" 'BEGIN{exit (m>=0 && m<=300)?0:1}' || SANITY_OK="no"
echo "sanity_ok: $SANITY_OK (0..300)"
[ "$SANITY_OK" = "yes" ] || { echo "RESULT=FAIL - invalid or out-of-range value"; exit 1; }

DUP_SAMPLE="$(printf '%s\n' "$BODY" | awk -F',' '{c[$1]++} END{for (d in c) if (c[d]>1) print d","c[d]}' | sort | head -n 5)"
DUP_COUNT="$(printf '%s\n' "$BODY" | awk -F',' '{c[$1]++} END{n=0;for (d in c) if (c[d]>1) n+=c[d]-1; print n}')"
echo "duplicate_date_rows: ${DUP_COUNT:-0}"
[ "${DUP_COUNT:-0}" -eq 0 ] || { echo "RESULT=FAIL - duplicate dates present"; exit 1; }

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

JOB_SNIPPET="$(adb shell dumpsys jobscheduler 2>/dev/null | head -n 200 | tr -d '\r' | awk -v pat="$EXPECTED_WORKER" '$0~pat{print; n=5; next} n>0{print; n--}')"
WORKER_OK="no"; [ -n "$JOB_SNIPPET" ] && WORKER_OK="yes"
echo "worker_expected: $EXPECTED_WORKER"
echo "worker_found: $WORKER_OK"
[ -n "$JOB_SNIPPET" ] && { echo "--- DEBUG: jobscheduler grep ---"; printf '%s\n' "$JOB_SNIPPET" | sed 's/^/  /'; }

if [ "$WORKER_OK" = "yes" ]; then
  echo "RESULT=PASS"; exit 0
else
  echo "RESULT=FAIL - expected worker not found"; exit 1
fi
