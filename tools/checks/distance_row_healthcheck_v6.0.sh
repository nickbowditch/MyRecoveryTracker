#!/bin/bash
set -eu
PKG="com.nick.myrecoverytracker"
CSV="files/daily_distance_log.csv"
EXPECTED_WORKER="DistanceWorker"
EXPECTED_REDCAP_FIELD="distance_m"
OUT="evidence/v6.0/movement/distance_row_healthcheck.txt"
mkdir -p "$(dirname "$OUT")"
exec >"$OUT" 2>&1
adb get-state >/dev/null 2>&1 || { echo "RESULT=FAIL (no device)"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "RESULT=FAIL (app not installed)"; exit 3; }
TODAY="$(adb shell 'toybox date +%F 2>/dev/null || date +%F' | tr -d '\r')"
if ! adb exec-out run-as "$PKG" sh -c "test -f '$CSV'" >/dev/null 2>&1; then
  echo "RESULT=FAIL (no CSV found)"
  exit 1
fi
HEADER="$(adb exec-out run-as "$PKG" head -n 1 "$CSV" | tr -d '\r')"
BODY="$(adb exec-out run-as "$PKG" tail -n +2 "$CSV" | tr -d '\r' | sed '/^[[:space:]]*$/d')"
echo "--- DEBUG: header & tail ---"
echo "  $HEADER"
printf '%s\n' "$BODY" | tail -n 5 | sed 's/^/  /'
HEADER_OK="no"
printf '%s' "$HEADER" | awk -F',' '
  BEGIN{ok=0}
  {
    gsub(/^[ \t]+|[ \t]+$/,"",$1); gsub(/^[ \t]+|[ \t]+$/,"",$2);
    if (tolower($1)=="date" && tolower($2)=="distance_km") ok=1
  }
  END{ exit ok?0:1 }' && HEADER_OK="yes"
echo "header_ok: $HEADER_OK"
ROWS_TODAY="$(printf '%s\n' "$BODY" | awk -F',' -v d="$TODAY" 'NF>1 && $1==d {c++} END{print c+0}')"
echo "rows_today: $ROWS_TODAY"
TODAY_VALS="$(printf '%s\n' "$BODY" | awk -F',' -v d="$TODAY" 'NF>1 && $1==d {print $2}')"
SANITY_OK="yes"
if [ -z "$TODAY_VALS" ]; then
  SANITY_OK="no"
else
  echo "$TODAY_VALS" | while IFS= read -r km; do
    val="$(printf '%s' "$km" | tr -d '[:space:]')"
    echo "$val" | grep -Eq '^[0-9]+([.][0-9]+)?$' || { SANITY_OK="no"; break; }
    awk -v x="$val" 'BEGIN{ exit (x>=0 && x<=200)?0:1 }' || { SANITY_OK="no"; break; }
  done
fi
echo "today_values_km: ${TODAY_VALS:-<none>}"
echo "sanity_ok: $SANITY_OK (0..200 km)"
LAST3="$(printf '%s\n' "$BODY" | awk -F',' 'NF>1{d=$1;if(!seen[d]++){print d}}' | tail -n 3)"
echo "last_3_days:"
if [ -n "$LAST3" ]; then
  YDAY="$(printf '%s\n' "$LAST3" | awk -v t="$TODAY" 't!=$0{last=$0} END{print last}')"
  printf '%s\n' "$LAST3" | while IFS= read -r d; do
    [ -z "$d" ] && continue
    label="$d"
    [ "$d" = "$TODAY" ] && label="today"
    [ "$d" = "$YDAY" ] && [ "$d" != "$TODAY" ] && label="yesterday"
    v="$(printf '%s\n' "$BODY" | awk -F',' -v D="$d" 'NF>1 && $1==D {print $2; exit}')"
    printf "  - %s : %s\n" "$label" "${v:-<no row>}"
  done
fi
JOB_SNIPPET="$(adb shell dumpsys jobscheduler 2>/dev/null | tr -d '\r' | awk -v pat="$EXPECTED_WORKER" '$0~pat{print; n=6; next} n>0{print; n--}')"
WORKER_OK="no"; [ -n "$JOB_SNIPPET" ] && WORKER_OK="yes"
echo "worker_expected: $EXPECTED_WORKER"
echo "worker_found: $WORKER_OK"
[ -n "$JOB_SNIPPET" ] && { echo "--- DEBUG: jobscheduler grep ---"; printf '%s\n' "$JOB_SNIPPET" | sed 's/^/  /'; }
REDCAP_FIELD_OK="skip"
REDCAP_TODAY="skip"
if [ -n "${REDCAP_API_URL:-}" ] && [ -n "${REDCAP_API_TOKEN:-}" ]; then
  REDCAP_FIELD_OK="$(curl -sS -X POST "$REDCAP_API_URL" \
    -d "token=$REDCAP_API_TOKEN" -d "content=metadata" -d "format=json" \
    | jq -r --arg f "$EXPECTED_REDCAP_FIELD" '[.[]|select(.field_name==$f)]|if length>0 then "yes" else "no" end')"
  RESP="$(curl -sS -X POST "$REDCAP_API_URL" \
    -d "token=$REDCAP_API_TOKEN" -d "content=record" -d "format=json" -d "type=flat" \
    -d "fields[]=record_id" -d "fields[]=date" -d "fields[]=$EXPECTED_REDCAP_FIELD" \
    --data-urlencode "filterLogic=[date]='$TODAY' and [$EXPECTED_REDCAP_FIELD]<>''")"
  COUNT="$(printf '%s' "$RESP" | jq 'length')"
  [ "${COUNT:-0}" -gt 0 ] && REDCAP_TODAY="present" || REDCAP_TODAY="none"
fi
echo "redcap_field_exists($EXPECTED_REDCAP_FIELD): $REDCAP_FIELD_OK"
echo "redcap_today_status: $REDCAP_TODAY"
RC=0
[ "$HEADER_OK" = "yes" ] || RC=1
[ "$ROWS_TODAY" -gt 0 ] || RC=1
[ "$SANITY_OK" = "yes" ] || RC=1
[ "$WORKER_OK" = "yes" ] || RC=1
if [ "$RC" -eq 0 ]; then
  echo "RESULT=PASS"
else
  echo "RESULT=FAIL"
fi
exit "$RC"
