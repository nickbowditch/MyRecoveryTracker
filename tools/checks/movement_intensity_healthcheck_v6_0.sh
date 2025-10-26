#!/bin/sh
set -eu

PKG="com.nick.myrecoverytracker"
CSV="files/daily_movement_intensity.csv"
EXPECTED_WORKER="MovementIntensityDailyWorker"
EXPECTED_REDCAP_FIELD="move_intensity_score"
VALUE_MIN=0
VALUE_MAX=100

pass() { echo "RESULT=PASS"; exit 0; }
fail() { echo "RESULT=FAIL - $1"; debug; exit 1; }
debug() {
  echo "--- DEBUG: header & tail ---"
  adb exec-out run-as "$PKG" head -n 1 "$CSV" 2>/dev/null | tr -d '\r' | sed 's/^/  /' || true
  adb exec-out run-as "$PKG" tail -n 5 "$CSV" 2>/dev/null | tr -d '\r' | sed 's/^/  /' || true
  echo "--- DEBUG: jobscheduler grep ---"
  adb shell dumpsys jobscheduler 2>/dev/null | tr -d '\r' | awk -v p="$EXPECTED_WORKER" '
    $0 ~ p {print; c=4; next} c>0 {print; c--}
  ' | sed 's/^/  /' || true
  if [ -n "${REDCAP_API_URL:-}" ] && [ -n "${REDCAP_API_TOKEN:-}" ]; then
    echo "--- DEBUG: REDCap field lookup ---"
    curl -sS -X POST "$REDCAP_API_URL" -d "token=$REDCAP_API_TOKEN" -d "content=metadata" -d "format=json" \
      | jq -r --arg f "$EXPECTED_REDCAP_FIELD" '[.[]|select(.field_name==$f)]|length' 2>/dev/null | sed 's/^/  /' || true
  fi
}

adb get-state >/dev/null 2>&1 || { echo "RESULT=FAIL - no device"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "RESULT=FAIL - app not installed"; exit 3; }

TODAY="$(adb shell 'toybox date +%F 2>/dev/null || date +%F' | tr -d '\r')"

adb exec-out run-as "$PKG" sh -c "test -f $CSV" >/dev/null 2>&1 || fail "CSV missing: $CSV"

HEADER="$(adb exec-out run-as "$PKG" head -n 1 "$CSV" | tr -d '\r')"
[ -n "$HEADER" ] || fail "empty header"

VAL_IDX="$(printf '%s\n' "$HEADER" | awk -F',' '
  BEGIN{idx=0}
  {
    for(i=1;i<=NF;i++){
      n=$i; gsub(/^[ \t]+|[ \t]+$/,"",n)
      if(n=="move_intensity_score"||n=="movement_intensity"||n=="intensity"){idx=i;break}
    }
    print idx
  }'
)"
[ "$VAL_IDX" -gt 0 ] || fail "value column not found in header"

ROWS_TODAY="$(adb exec-out run-as "$PKG" awk -F',' -v d="$TODAY" 'NR>1 && $1==d {c++} END{print c+0}' "$CSV" | tr -d '\r')"
echo "rows_today: $ROWS_TODAY"
[ "$ROWS_TODAY" -gt 0 ] || fail "no rows for today"

TODAY_VALS="$(adb exec-out run-as "$PKG" awk -F',' -v d="$TODAY" -v i="$VAL_IDX" 'NR>1 && $1==d && NF>=i {print $i}' "$CSV" | tr -d '\r')"
[ -n "$TODAY_VALS" ] || fail "no values for today"

SANITY_OK="yes"
printf '%s\n' "$TODAY_VALS" | while IFS= read -r v; do
  vt="$(printf '%s' "$v" | tr -d '[:space:]')"
  echo "$vt" | grep -Eq '^-?[0-9]+([.][0-9]+)?$' || SANITY_OK="no"
  awk -v x="$vt" -v lo="$VALUE_MIN" -v hi="$VALUE_MAX" 'BEGIN{exit !(x>=lo && x<=hi)}' || SANITY_OK="no"
done
[ "$SANITY_OK" = "yes" ] || fail "today values not numeric or out of range [$VALUE_MIN,$VALUE_MAX]"

DATES_DISTINCT="$(adb exec-out run-as "$PKG" awk -F',' 'NR>1 && $1!="" {print $1}' "$CSV" | tr -d '\r' | awk '!seen[$0]++')"
LAST3="$(printf '%s\n' "$DATES_DISTINCT" | tail -n 3)"
echo "last_3_days:"
if [ -n "$LAST3" ]; then
  CNT="$(printf '%s\n' "$LAST3" | wc -l | tr -d ' ')"
  i=1
  printf '%s\n' "$LAST3" | while IFS= read -r d; do
    [ -z "$d" ] && continue
    label="$d"
    [ "$d" = "$TODAY" ] && label="today"
    if [ "$label" = "$d" ] && [ "$i" -eq "$CNT" ]; then
      # most recent prior to today appears last in LAST3 if today present
      if [ "$d" != "$TODAY" ]; then label="yesterday"; fi
    fi
    v="$(adb exec-out run-as "$PKG" awk -F',' -v D="$d" -v i="$VAL_IDX" 'NR>1 && $1==D && NF>=i {print $i; exit}' "$CSV" | tr -d '\r')"
    printf "  - %s : %s\n" "$label" "${v:-<no row>}"
    i=$((i+1))
  done
fi

JOBDUMP="$(adb shell dumpsys jobscheduler 2>/dev/null | tr -d '\r' || true)"
WORKER_OK="no"
printf '%s\n' "$JOBDUMP" | grep -Fq "$EXPECTED_WORKER" && WORKER_OK="yes"
echo "worker_expected: $EXPECTED_WORKER"
echo "worker_found: $WORKER_OK"
[ "$WORKER_OK" = "yes" ] || fail "expected worker not found in jobscheduler"

REDCAP_STATUS="skipped"
REDCAP_FIELD_OK="skipped"
REDCAP_TODAY="skipped"
if [ -n "${REDCAP_API_URL:-}" ] && [ -n "${REDCAP_API_TOKEN:-}" ]; then
  REDCAP_FIELD_OK="$(curl -sS -X POST "$REDCAP_API_URL" -d "token=$REDCAP_API_TOKEN" -d "content=metadata" -d "format=json" \
    | jq -r --arg f "$EXPECTED_REDCAP_FIELD" '[.[]|select(.field_name==$f)]|if length>0 then "yes" else "no" end' 2>/dev/null || echo no)"
  RESP="$(curl -sS -X POST "$REDCAP_API_URL" -d "token=$REDCAP_API_TOKEN" -d "content=record" -d "format=json" -d "type=flat" \
    -d "fields[]=record_id" -d "fields[]=date" -d "fields[]=$EXPECTED_REDCAP_FIELD" \
    --data-urlencode "filterLogic=[date]='$TODAY' and [$EXPECTED_REDCAP_FIELD]<>''" 2>/dev/null || echo '[]')"
  COUNT="$(printf '%s\n' "$RESP" | jq 'length' 2>/dev/null || echo 0)"
  [ "$COUNT" -gt 0 ] && REDCAP_TODAY="present" || REDCAP_TODAY="none"
  REDCAP_STATUS="checked"
fi
echo "redcap_field_exists($EXPECTED_REDCAP_FIELD): $REDCAP_FIELD_OK"
echo "redcap_today_status: $REDCAP_TODAY"
echo "notes: VALUE_RANGE=${VALUE_MIN}-${VALUE_MAX}; CSV=$CSV; TODAY=$TODAY"

if [ "$REDCAP_STATUS" = "checked" ]; then
  [ "$REDCAP_FIELD_OK" = "yes" ] || fail "redcap field missing"
  [ "$REDCAP_TODAY" = "present" ] || fail "redcap has no row for today"
fi

pass
