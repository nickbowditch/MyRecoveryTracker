#!/bin/sh
set -eu

PKG="com.nick.myrecoverytracker"
CSV="files/daily_notification_latency.csv"
EXPECTED_WORKER="NotificationLatencyWorker"
EXPECTED_REDCAP_FIELDS="notif_latency_avg_s,notif_latency_median_s,notif_latency_n"
VALUE_MIN_S=0
VALUE_MAX_S=3600
OUT="evidence/v6.0/attention/notification_latency_row_healthcheck_v6_0.txt"

mkdir -p "$(dirname "$OUT")"

echo "[INFO] Notification Latency Row Healthcheck v6.0"
echo "[INFO] PKG=$PKG"
echo "[INFO] CSV=$CSV"
echo "[INFO] EXPECTED_WORKER=$EXPECTED_WORKER"
echo "[INFO] EXPECTED_REDCAP_FIELDS=$EXPECTED_REDCAP_FIELDS"
echo "[INFO] VALUE_RANGE_S=${VALUE_MIN_S}-${VALUE_MAX_S}"

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

IDX_LINE="$(printf '%s\n' "$HEADER" | tr '[:upper:]' '[:lower:]' | awk -F',' '
  {
    for(i=1;i<=NF;i++){
      gsub(/^[ \t]+|[ \t]+$/,"",$i)
      n=$i
      if(n=="avg_s" || n=="average_s" || n=="latency_avg_s" || n=="avg") ai=i
      if(n=="median_s" || n=="latency_median_s" || n=="median") mi=i
      if(n=="n" || n=="count" || n=="samples" || n=="latency_n") ni=i
    }
    printf "ai=%d mi=%d ni=%d\n", ai+0, mi+0, ni+0
  }')"
AI="$(printf '%s\n' "$IDX_LINE" | awk -F'[ =]+' '{for(i=1;i<=NF;i++) if($i=="ai") {print $(i+1); exit}}')"
MI="$(printf '%s\n' "$IDX_LINE" | awk -F'[ =]+' '{for(i=1;i<=NF;i++) if($i=="mi") {print $(i+1); exit}}')"
NI="$(printf '%s\n' "$IDX_LINE" | awk -F'[ =]+' '{for(i=1;i<=NF;i++) if($i=="ni") {print $(i+1); exit}}')"

[ "${AI:-0}" -gt 0 ] || { echo "RESULT=FAIL - avg_s column not found"; exit 1; }
[ "${MI:-0}" -gt 0 ] || { echo "RESULT=FAIL - median_s column not found"; exit 1; }
[ "${NI:-0}" -gt 0 ] || { echo "RESULT=FAIL - n column not found"; exit 1; }

ROWS_TODAY="$(printf '%s\n' "$BODY" | awk -F',' -v d="$TODAY" 'NF>0 && $1==d {c++} END{print c+0}')"
echo "rows_today: $ROWS_TODAY"
[ "$ROWS_TODAY" -gt 0 ] || { echo "RESULT=FAIL - no rows for today"; exit 1; }

TODAY_LINE="$(printf '%s\n' "$BODY" | awk -F',' -v d="$TODAY" 'NF>0 && $1==d {print; exit}')"
AVG_S="$(printf '%s\n' "$TODAY_LINE" | awk -F',' -v i="$AI" 'NF>=i{print $i}')"
MED_S="$(printf '%s\n' "$TODAY_LINE" | awk -F',' -v i="$MI" 'NF>=i{print $i}')"
N_VAL="$(printf '%s\n' "$TODAY_LINE" | awk -F',' -v i="$NI" 'NF>=i{print $i}')"
echo "today_values: avg_s=${AVG_S:-<none>}  median_s=${MED_S:-<none>}  n=${N_VAL:-<none>}"

SANITY_OK="yes"
echo "$AVG_S" | grep -Eq '^[0-9]+([.][0-9]+)?$' || SANITY_OK="no"
echo "$MED_S" | grep -Eq '^[0-9]+([.][0-9]+)?$' || SANITY_OK="no"
echo "$N_VAL" | grep -Eq '^[0-9]+$' || SANITY_OK="no"
[ "$SANITY_OK" = "yes" ] && \
  awk -v a="$AVG_S" -v m="$MED_S" -v lo="$VALUE_MIN_S" -v hi="$VALUE_MAX_S" 'BEGIN{exit (a>=lo && a<=hi && m>=lo && m<=hi)?0:1}' || SANITY_OK="no"
echo "sanity_ok: $SANITY_OK (${VALUE_MIN_S}..${VALUE_MAX_S}s, n>=0)"
[ "$SANITY_OK" = "yes" ] || { echo "RESULT=FAIL - invalid values/range"; exit 1; }

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
fi

JOB_SNIPPET="$(adb shell dumpsys jobscheduler 2>/dev/null | head -n 200 | tr -d '\r' | awk -v pat="$EXPECTED_WORKER" '$0~pat{print; n=5; next} n>0{print; n--}')"
WORKER_OK="no"; [ -n "$JOB_SNIPPET" ] && WORKER_OK="yes"
echo "worker_expected: $EXPECTED_WORKER"
echo "worker_found: $WORKER_OK"
[ -n "$JOB_SNIPPET" ] && { echo "--- DEBUG: jobscheduler grep ---"; printf '%s\n' "$JOB_SNIPPET" | sed 's/^/  /'; }
[ "$WORKER_OK" = "yes" ] || { echo "RESULT=FAIL - expected worker not found"; exit 1; }

REDCAP_FIELDS_STATUS="skipped"
REDCAP_TODAY_STATUS="skipped"
if [ -n "${REDCAP_API_URL:-}" ] && [ -n "${REDCAP_API_TOKEN:-}" ]; then
  REDCAP_FIELDS_STATUS="$(curl -sS -X POST "$REDCAP_API_URL" \
    -d "token=$REDCAP_API_TOKEN" -d "content=metadata" -d "format=json" \
    | jq -r --arg a "notif_latency_avg_s" --arg b "notif_latency_median_s" --arg c "notif_latency_n" \
      '[(.[]|select(.field_name==$a)),(.[]|select(.field_name==$b)),(.[]|select(.field_name==$c))] | (if (length==3) then "yes" else "no" end)')"
  RESP="$(curl -sS -X POST "$REDCAP_API_URL" \
    -d "token=$REDCAP_API_TOKEN" -d "content=record" -d "format=json" -d "type=flat" \
    -d "fields[]=record_id" -d "fields[]=date" \
    -d "fields[]=notif_latency_avg_s" -d "fields[]=notif_latency_median_s" -d "fields[]=notif_latency_n" \
    --data-urlencode "filterLogic=[date]='$TODAY' and ([notif_latency_avg_s]<>'' or [notif_latency_median_s]<>'' or [notif_latency_n]<>'' )")"
  COUNT="$(printf '%s\n' "$RESP" | jq 'length' 2>/dev/null || echo 0)"
  [ "$COUNT" -gt 0 ] && REDCAP_TODAY_STATUS="present" || REDCAP_TODAY_STATUS="none"
fi
echo "redcap_field_exists(all): $REDCAP_FIELDS_STATUS"
echo "redcap_today_status: $REDCAP_TODAY_STATUS"

echo "RESULT=PASS"
exit 0
