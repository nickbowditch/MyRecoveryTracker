#!/bin/bash
set -euo pipefail

PKG="com.nick.myrecoverytracker"
REDCAP_API_URL="https://redcap.une.edu.au/api/"
REDCAP_API_TOKEN="B717098DF6799DBE0406F0DE0080CDA8"
OUT="evidence/v6.0/_redcap/healthcheck.txt"

command -v jq >/dev/null 2>&1 || { echo "REDCAP RESULT=FAIL (jq missing)"; exit 1; }

read -r -d '' FEATURES <<'CFG'
files/daily_movement_intensity.csv | movement_intensity_daily | date | intensity    | 0 | 1
files/daily_distance_log.csv       | distance_daily            | date | meters       | 0 | 1
files/daily_usage_events.csv       | usage_events_daily        | date | event_count  | 0 | 1
CFG

mkdir -p "$(dirname "$OUT")"
: > "$OUT"

api_export_records(){
  local form="$1" date_field="$2" date_val="$3" value_field="$4"
  curl -sS -X POST "$REDCAP_API_URL" \
    -F "token=$REDCAP_API_TOKEN" \
    -F "content=record" \
    -F "format=json" \
    -F "type=flat" \
    -F "forms[]=$form" \
    -F "fields[]=record_id" \
    -F "fields[]=$date_field" \
    -F "fields[]=$value_field" \
    -F "filterLogic=[${date_field}]='${date_val}'"
}

today_ymd(){
  if command -v adb >/dev/null 2>&1 && adb get-state >/dev/null 2>&1; then
    adb shell 'toybox date +%F 2>/dev/null || date +%F' 2>/dev/null | tr -d $'\r'
  else
    date +%F
  fi
}

get_device_value(){ # csv_path, yyyy-mm-dd, value_field
  local csv="$1" ymd="$2" vfield="$3"
  local hdr idx
  hdr="$(adb exec-out run-as "$PKG" sed -n '1p' "$csv" 2>/dev/null | tr -d $'\r' || true)"
  [ -n "$hdr" ] || { echo ""; return; }
  idx="$(awk -F',' -v f="$vfield" 'NR==1{for(i=1;i<=NF;i++) if($i==f){print i; exit}}' <(printf '%s\n' "$hdr"))"
  [ -n "$idx" ] || { echo ""; return; }
  adb exec-out run-as "$PKG" awk -F',' -v d="$ymd" -v i="$idx" 'NR>1 && $1==d{print $i; exit}' "$csv" 2>/dev/null | tr -d $'\r'
}

absdiff(){ awk -v a="$1" -v b="$2" 'BEGIN{d=a-b; if(d<0)d=-d; print d}'; }

START="$(date -u +'%Y-%m-%dT%H:%M:%SZ')"
TODAY="$(today_ymd)"
[ -n "$TODAY" ] || { echo "REDCAP RESULT=FAIL (no date)"; exit 1; }

API_PING="$(curl -sS -X POST "$REDCAP_API_URL" -F "token=$REDCAP_API_TOKEN" -F "content=project" -F "format=json" || true)"
echo "$API_PING" | jq -e '.project_id' >/dev/null 2>&1 || { echo "REDCAP RESULT=FAIL (API/token check failed)"; exit 1; }

PASS=0; FAIL=0; SKIP=0
echo "started_at=$START today=$TODAY" >> "$OUT"

while IFS='|' read -r raw_csv raw_form raw_date raw_val raw_tol raw_en; do
  line="$(printf '%s' "$raw_csv" | sed 's/^[[:space:]]*//;s/[[:space:]]*$//')"
  [ -n "$line" ] || continue
  case "$line" in \#*) continue;; esac

  csv="$(echo "$raw_csv" | xargs)"
  form="$(echo "$raw_form" | xargs)"
  date_field="$(echo "$raw_date" | xargs)"
  val_field="$(echo "$raw_val" | xargs)"
  tol="$(echo "$raw_tol" | xargs)"
  en="$(echo "$raw_en" | xargs)"

  if [ "${en:-0}" != "1" ]; then
    echo "$form: SKIP" >> "$OUT"; SKIP=$((SKIP+1)); continue
  fi

  LVAL="$(get_device_value "$csv" "$TODAY" "$val_field" || true)"
  printf '%s' "$LVAL" | grep -Eq '^-?[0-9]+(\.[0-9]+)?$' || { echo "$form: FAIL (local value missing/non-numeric)" >> "$OUT"; FAIL=$((FAIL+1)); continue; }

  RESP="$(api_export_records "$form" "$date_field" "$TODAY" "$val_field" || true)"
  COUNT="$(printf '%s' "$RESP" | jq 'length' 2>/dev/null || echo 0)"
  [ "${COUNT:-0}" -ge 1 ] || { echo "$form: FAIL (no REDCap rows for $TODAY)" >> "$OUT"; FAIL=$((FAIL+1)); continue; }

  RVAL="$(printf '%s' "$RESP" | jq -r ".[0].${val_field} // empty" 2>/dev/null || true)"
  printf '%s' "$RVAL" | grep -Eq '^-?[0-9]+(\.[0-9]+)?$' || { echo "$form: FAIL (REDCap value missing/non-numeric)" >> "$OUT"; FAIL=$((FAIL+1)); continue; }

  DIFF="$(absdiff "$LVAL" "$RVAL")"
  if awk -v d="$DIFF" -v t="$tol" 'BEGIN{exit !(d<=t)}'; then
    echo "$form: PASS (local=$LVAL redcap=$RVAL tol=$tol)" >> "$OUT"
    PASS=$((PASS+1))
  else
    echo "$form: FAIL (mismatch local=$LVAL redcap=$RVAL tol=$tol)" >> "$OUT"
    FAIL=$((FAIL+1))
  fi
done <<< "$FEATURES"

TOTAL=$((PASS+FAIL+SKIP))
{
  echo
  echo "summary total=$TOTAL pass=$PASS fail=$FAIL skip=$SKIP"
  if [ "$FAIL" -eq 0 ]; then echo "REDCAP RESULT=PASS"; else echo "REDCAP RESULT=FAIL"; fi
} >> "$OUT"

[ "$FAIL" -eq 0 ]
