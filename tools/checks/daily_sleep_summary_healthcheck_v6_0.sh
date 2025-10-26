#!/bin/sh
set -eu

PKG="com.nick.myrecoverytracker"
CSV="files/daily_sleep_summary.csv"
EXPECTED_WORKER="SleepRollupWorker"
OUT="evidence/v6.0/sleep/daily_sleep_summary_healthcheck_v6_0.txt"

mkdir -p "$(dirname "$OUT")"

echo "[INFO] Daily Sleep Summary Healthcheck v6.0"
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
    if(h=="total_sleep_minutes"||h=="sleep_minutes_total"||h=="minutes_total"||h=="total_sleep_min") mi=i
    if(h=="sleep_efficiency"||h=="efficiency"||h=="sleep_eff") ei=i
  }
  printf "mi=%d ei=%d\n", mi+0, ei+0
}')"

MI="$(printf '%s\n' "$IDX_LINE" | awk -F'[ =]+' '{for(i=1;i<=NF;i++) if($i=="mi"){print $(i+1); exit}}')"
EI="$(printf '%s\n' "$IDX_LINE" | awk -F'[ =]+' '{for(i=1;i<=NF;i++) if($i=="ei"){print $(i+1); exit}}')"

[ "${MI:-0}" -gt 0 ] || { echo "RESULT=FAIL - total_sleep_minutes column not found"; exit 1; }
[ "${EI:-0}" -gt 0 ] || { echo "RESULT=FAIL - sleep_efficiency column not found"; exit 1; }

ROWS_TODAY="$(printf '%s\n' "$BODY" | awk -F',' -v d="$TODAY" 'NF>0 && $1==d {c++} END{print c+0}')"
echo "rows_today: $ROWS_TODAY"
[ "$ROWS_TODAY" -gt 0 ] || { echo "RESULT=FAIL - no rows for today"; exit 1; }

TODAY_LINE="$(printf '%s\n' "$BODY" | awk -F',' -v d="$TODAY" 'NF>0 && $1==d {print; exit}')"
MIN_VAL="$(printf '%s\n' "$TODAY_LINE" | awk -F',' -v i="$MI" 'NF>=i{print $i}')"
EFF_VAL="$(printf '%s\n' "$TODAY_LINE" | awk -F',' -v i="$EI" 'NF>=i{print $i}')"
echo "today_values: total_sleep_minutes=${MIN_VAL:-<none>} sleep_efficiency=${EFF_VAL:-<none>} (expected ranges: minutes 0..900, efficiency 0..1)"

SANITY_OK="yes"
echo "$MIN_VAL" | grep -Eq '^[0-9]+([.][0-9]+)?$' || SANITY_OK="no"
echo "$EFF_VAL" | grep -Eq '^[0-9]+([.][0-9]+)?$' || SANITY_OK="no"
[ "$SANITY_OK" = "yes" ] && awk -v m="$MIN_VAL" -v e="$EFF_VAL" 'BEGIN{exit (m>=0 && m<=900 && e>=0 && e<=1)?0:1}' || SANITY_OK="no"
echo "sanity_ok: $SANITY_OK (minutes 0..900, efficiency 0..1)"
[ "$SANITY_OK" = "yes" ] || { echo "RESULT=FAIL - values out of range or non-numeric"; exit 1; }

DUP_SAMPLE="$(printf '%s\n' "$BODY" | awk -F',' '{c[$1]++} END{for (d in c) if (c[d]>1) print d","c[d]}' | sort | head -n 5)"
DUP_COUNT="$(printf '%s\n' "$BODY" | awk -F',' '{c[$1]++} END{n=0;for (d in c) if (c[d]>1) n+=c[d]-1; print n}')"
echo "duplicate_date_rows: ${DUP_COUNT:-0}"
[ -n "$DUP_SAMPLE" ] && { echo "--- DEBUG: duplicate sample (date,count) ---"; printf '%s\n' "$DUP_SAMPLE" | sed 's/^/  /'; }
[ "${DUP_COUNT:-0}" -eq 0 ] || { echo "RESULT=FAIL - duplicate dates present"; exit 1; }

LAST3="$(printf '%s\n' "$BODY" | awk -F',' 'NF>0{d=$1;if(!seen[d]++){print d}}' | tail -n 3)"
echo "last_3_days:"
if [ -n "$LAST3" ]; then
  YDAY="$(printf '%s\n' "$LAST3" | awk -v t="$TODAY" 't!=$0{last=$0} END{print last}')"
  printf '%s\n' "$LAST3" | while IFS= read -r d; do
    [ -z "$d" ] && continue
    label="$d"
    [ "$d" = "$TODAY" ] && label="today"
    [ "$d" = "$YDAY" ] && [ "$d" != "$TODAY" ] && label="yesterday"
    line="$(printf '%s\n' "$BODY" | awk -F',' -v D="$d" 'NF>0 && $1==D {print $0; exit}')"
    printf "  - %s : %s\n" "$label" "${line:-<no row>}"
  done
else
  echo "  <no recent dates found>"
fi

JOB_SNIPPET="$(adb shell dumpsys jobscheduler 2>/dev/null | head -n 200 | tr -d '\r' | awk -v pat="$EXPECTED_WORKER" '$0~pat{print; n=5; next} n>0{print; n--}')"
WORKER_OK="no"; [ -n "$JOB_SNIPPET" ] && WORKER_OK="yes"
echo "worker_expected: $EXPECTED_WORKER"
echo "worker_found: $WORKER_OK"
[ -n "$JOB_SNIPPET" ] && { echo "--- DEBUG: jobscheduler grep ---"; printf '%s\n' "$JOB_SNIPPET" | sed 's/^/  /'; }
[ "$WORKER_OK" = "yes" ] || { echo "RESULT=FAIL - expected worker not found"; exit 1; }

echo "RESULT=PASS"
exit 0
