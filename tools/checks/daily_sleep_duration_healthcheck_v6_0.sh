#!/bin/sh
set -eu

PKG="com.nick.myrecoverytracker"
CSV="files/daily_sleep_duration.csv"
EXPECTED_WORKER="SleepDurationWorker"
VALUE_MIN_MIN=0
VALUE_MAX_MIN=900
OUT="evidence/v6.0/sleep/daily_sleep_duration_healthcheck_v6_0.txt"

mkdir -p "$(dirname "$OUT")"

echo "[INFO] Daily Sleep Duration Healthcheck v6.0"
echo "[INFO] PKG=$PKG"
echo "[INFO] CSV=$CSV"
echo "[INFO] EXPECTED_WORKER=$EXPECTED_WORKER"
echo "[INFO] VALUE_RANGE_MIN=${VALUE_MIN_MIN}-${VALUE_MAX_MIN}"

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
    if(h=="total_sleep_minutes"||h=="sleep_minutes"||h=="duration_minutes"||h=="sleep_min"||h=="minutes") mi=i
    if(h=="total_sleep_hours"||h=="sleep_hours"||h=="duration_hours"||h=="hours") hi=i
  }
  printf "mi=%d hi=%d\n", mi+0, hi+0
}')"
MI="$(printf '%s\n' "$IDX_LINE" | awk -F'[ =]+' '{for(i=1;i<=NF;i++) if($i=="mi"){print $(i+1); exit}}')"
HI="$(printf '%s\n' "$IDX_LINE" | awk -F'[ =]+' '{for(i=1;i<=NF;i++) if($i=="hi"){print $(i+1); exit}}')"

[ "${MI:-0}" -gt 0 -o "${HI:-0}" -gt 0 ] || { echo "RESULT=FAIL - no duration column (minutes or hours)"; exit 1; }

ROWS_TODAY="$(printf '%s\n' "$BODY" | awk -F',' -v d="$TODAY" '$1==d {c++} END{print c+0}')"
echo "rows_today: $ROWS_TODAY"
[ "$ROWS_TODAY" -gt 0 ] || { echo "RESULT=FAIL - no rows for today"; exit 1; }

TODAY_LINE="$(printf '%s\n' "$BODY" | awk -F',' -v d="$TODAY" '$1==d {print; exit}')"
VAL_MIN="$(printf '%s\n' "$TODAY_LINE" | awk -F',' -v i="${MI:-0}" 'NF>=i && i>0 {print $i}')"
VAL_HR="$(printf '%s\n' "$TODAY_LINE" | awk -F',' -v i="${HI:-0}" 'NF>=i && i>0 {print $i}')"

if [ -n "$VAL_MIN" ]; then
  EVAL_MIN="$VAL_MIN"
elif [ -n "$VAL_HR" ]; then
  EVAL_MIN="$(awk -v h="$VAL_HR" 'BEGIN{if(h+0!=h) exit 1; printf "%.2f", h*60}')" || { echo "RESULT=FAIL - non-numeric hours value"; exit 1; }
else
  echo "RESULT=FAIL - no duration value for today"; exit 1
fi

echo "today_values: minutes=${VAL_MIN:-<none>} hours=${VAL_HR:-<none>} eval_minutes=$EVAL_MIN"

SANITY_OK="yes"
echo "$EVAL_MIN" | grep -Eq '^[0-9]+([.][0-9]+)?$' || SANITY_OK="no"
[ "$SANITY_OK" = "yes" ] && awk -v m="$EVAL_MIN" -v lo="$VALUE_MIN_MIN" -v hi="$VALUE_MAX_MIN" 'BEGIN{exit (m>=lo && m<=hi)?0:1}' || SANITY_OK="no"
echo "sanity_ok: $SANITY_OK (${VALUE_MIN_MIN}..${VALUE_MAX_MIN} min)"
[ "$SANITY_OK" = "yes" ] || { echo "RESULT=FAIL - duration out of range or non-numeric"; exit 1; }

DUP_SAMPLE="$(printf '%s\n' "$BODY" | awk -F',' '{c[$1]++} END{for (d in c) if (c[d]>1) print d","c[d]}' | sort | head -n 5)"
DUP_COUNT="$(printf '%s\n' "$BODY" | awk -F',' '{c[$1]++} END{n=0;for (d in c) if (c[d]>1) n+=c[d]-1; print n}')"
echo "duplicate_date_rows: ${DUP_COUNT:-0}"
[ -n "$DUP_SAMPLE" ] && { echo "--- DEBUG: duplicate sample (date,count) ---"; printf '%s\n' "$DUP_SAMPLE" | sed 's/^/  /'; }
[ "${DUP_COUNT:-0}" -eq 0 ] || { echo "RESULT=FAIL - duplicate dates present"; exit 1; }

LAST3="$(printf '%s\n' "$BODY" | awk -F',' '{d=$1;if(!seen[d]++){print d}}' | tail -n 3)"
echo "last_3_days:"
if [ -n "$LAST3" ]; then
  YDAY="$(printf '%s\n' "$LAST3" | awk -v t="$TODAY" 't!=$0{last=$0} END{print last}')"
  printf '%s\n' "$LAST3" | while IFS= read -r d; do
    [ -z "$d" ] && continue
    label="$d"
    [ "$d" = "$TODAY" ] && label="today"
    [ "$d" = "$YDAY" ] && [ "$d" != "$TODAY" ] && label="yesterday"
    line="$(printf '%s\n' "$BODY" | awk -F',' -v D="$d" '$1==D {print $0; exit}')"
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
