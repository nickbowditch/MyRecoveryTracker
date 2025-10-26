#!/bin/sh
set -eu

PKG="com.nick.myrecoverytracker"
CSV="files/daily_movement_intensity.csv"
EXPECTED_WORKER="MovementIntensityDailyWorker"
EXPECTED_REDCAP_FIELD="move_intensity_score"
VALUE_MIN=0
VALUE_MAX=100
OUT="evidence/v6.0/movement/movement_intensity_healthcheck.txt"

mkdir -p "$(dirname "$OUT")"
exec > >(tee "$OUT") 2>&1

echo "[INFO] Movement Intensity Healthcheck v6.0"
adb get-state >/dev/null 2>&1 || { echo "RESULT=FAIL (no device)"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "RESULT=FAIL (app not installed)"; exit 3; }

TODAY="$(adb shell 'toybox date +%F 2>/dev/null || date +%F' | tr -d '\r')"

if ! adb exec-out run-as "$PKG" sh -c "test -f $CSV" >/dev/null 2>&1; then
  echo "RESULT=FAIL (missing CSV: $CSV)"
  exit 1
fi

HEADER="$(adb exec-out run-as "$PKG" head -n 1 "$CSV" | tr -d '\r')"
DATA="$(adb exec-out run-as "$PKG" tail -n +2 "$CSV" | tr -d '\r' | sed '/^[[:space:]]*$/d')"

VAL_IDX="$(printf '%s\n' "$HEADER" | awk -F',' '
  BEGIN{idx=0}
  {
    for(i=1;i<=NF;i++){
      n=$i; gsub(/^[ \t]+|[ \t]+$/,"",n)
      if(n=="move_intensity_score"||n=="movement_intensity"||n=="intensity"){idx=i;break}
    }
    print idx
  }')"

if [ "$VAL_IDX" -le 0 ]; then
  echo "RESULT=FAIL (no value column)"
  exit 1
fi

ROWS_TODAY="$(printf '%s\n' "$DATA" | awk -F',' -v d="$TODAY" 'NF>1 && $1==d {c++} END{print c+0}')"
LAST_DATE="$(printf '%s\n' "$DATA" | awk -F',' 'NF>1 {last=$1} END{print last}')"
TODAY_VALS="$(printf '%s\n' "$DATA" | awk -F',' -v d="$TODAY" -v i="$VAL_IDX" 'NF>=i && $1==d {print $i}')"

SANITY_OK="yes"
if [ -z "$TODAY_VALS" ]; then
  SANITY_OK="no"
else
  printf '%s\n' "$TODAY_VALS" | while IFS= read -r v; do
    vt="$(printf '%s' "$v" | tr -d '[:space:]')"
    echo "$vt" | grep -Eq '^-?[0-9]+(\.[0-9]+)?$' || SANITY_OK="no"
    awk -v x="$vt" -v lo="$VALUE_MIN" -v hi="$VALUE_MAX" 'BEGIN{exit !(x>=lo && x<=hi)}' || SANITY_OK="no"
  done || true
fi

readarray -t LAST3 <<EOF2
$(printf '%s\n' "$DATA" | awk -F',' 'NF>1{print $1}' | awk '!seen[$0]++' | tail -n 3)
EOF2

WORKER_STATUS="$(adb shell dumpsys jobscheduler 2>/dev/null | grep -A4 -F "$EXPECTED_WORKER" || true)"
WORKER_OK="no"
[ -n "$WORKER_STATUS" ] && WORKER_OK="yes"

REDCAP_MAP="skip"
REDCAP_TODAY="skip"
if [ -n "${REDCAP_API_URL:-}" ] && [ -n "${REDCAP_API_TOKEN:-}" ]; then
  REDCAP_MAP="$(curl -sS -X POST "$REDCAP_API_URL" \
    -d "token=$REDCAP_API_TOKEN" -d "content=metadata" -d "format=json" \
    | jq -r --arg f "$EXPECTED_REDCAP_FIELD" '[.[]|select(.field_name==$f)]|if length>0 then "yes" else "no" end')"
  RESP="$(curl -sS -X POST "$REDCAP_API_URL" \
    -d "token=$REDCAP_API_TOKEN" -d "content=record" -d "format=json" -d "type=flat" \
    -d "fields[]=record_id" -d "fields[]=date" -d "fields[]=$EXPECTED_REDCAP_FIELD" \
    --data-urlencode "filterLogic=[date]='$TODAY' and [$EXPECTED_REDCAP_FIELD]<>''")"
  COUNT="$(printf '%s' "$RESP" | jq 'length')"
  [ "${COUNT:-0}" -gt 0 ] && REDCAP_TODAY="present" || REDCAP_TODAY="none"
fi

echo "=== Movement :: daily_movement_intensity.csv ==="
echo "rows_today: $ROWS_TODAY"
echo "last_written_date: ${LAST_DATE:-<none>}"
echo "today_values: ${TODAY_VALS:-<none>}"
echo "sanity_ok: $SANITY_OK (range [$VALUE_MIN,$VALUE_MAX])"
echo "last_3_days:"
for d in "${LAST3[@]}"; do
  [ -z "$d" ] && continue
  v="$(printf '%s\n' "$DATA" | awk -F',' -v D="$d" -v i="$VAL_IDX" 'NF>=i && $1==D {print $i; exit}')"
  printf "  - %s : %s\n" "$d" "${v:-<no row>}"
done
echo "worker_expected: $EXPECTED_WORKER"
echo "worker_found: $WORKER_OK"
[ -n "$WORKER_STATUS" ] && echo "$WORKER_STATUS" | sed 's/^/  /'
echo "redcap_field_exists($EXPECTED_REDCAP_FIELD): $REDCAP_MAP"
echo "redcap_today_status: $REDCAP_TODAY"

if [ "$ROWS_TODAY" -gt 0 ] && [ "$SANITY_OK" = "yes" ] && [ "$WORKER_OK" = "yes" ]; then
  echo "RESULT=PASS"
  exit 0
else
  echo "RESULT=FAIL"
  exit 1
fi
