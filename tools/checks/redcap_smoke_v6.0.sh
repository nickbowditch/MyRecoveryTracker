#!/bin/sh
set -eu
OUT="evidence/v6.0/_redcap/smoke.txt"
mkdir -p "$(dirname "$OUT")"
fail(){ echo "REDCAP-SMOKE RESULT=FAIL ($1)" | tee "$OUT"; exit 1; }

: "${REDCAP_API_URL:?Missing REDCAP_API_URL}"
: "${REDCAP_API_TOKEN:?Missing REDCAP_API_TOKEN}"
command -v jq >/dev/null 2>&1 || fail "jq not installed"

PING="$(curl -sS -X POST "$REDCAP_API_URL" -F "token=$REDCAP_API_TOKEN" -F "content=project" -F "format=json" || true)"
echo "$PING" | jq -e '.project_id' >/dev/null 2>&1 || fail "API/token check failed"

CSV="files/daily_movement_intensity.csv"
[ -s "$CSV" ] || fail "missing $CSV"

TODAY="$(date +%F)"
LVAL="$(awk -F, -v d="$TODAY" 'NR==1{for(i=1;i<=NF;i++)if($i=="intensity")v=i;next} $1==d{print $v;exit}' "$CSV" 2>/dev/null || true)"
printf '%s' "${LVAL:-}" | grep -Eq '^-?[0-9]+(\.[0-9]+)?$' || fail "local value missing/non-numeric"

RESP="$(curl -sS -X POST "$REDCAP_API_URL" \
  -F "token=$REDCAP_API_TOKEN" -F "content=record" -F "format=json" -F "type=flat" \
  -F "fields[]=record_id" -F "fields[]=date" -F "fields[]=intensity" \
  --data-urlencode "filterLogic=[date]='$TODAY'" || true)"
RVAL="$(printf '%s' "$RESP" | jq -r '.[0].intensity // empty' 2>/dev/null || true)"
printf '%s' "${RVAL:-}" | grep -Eq '^-?[0-9]+(\.[0-9]+)?$' || fail "no REDCap value for today"

DIFF="$(awk -v a="$LVAL" -v b="$RVAL" 'BEGIN{d=a-b;if(d<0)d=-d;print d}')"
awk -v d="$DIFF" 'BEGIN{exit (d<=2)?0:1}' || fail "mismatch local=$LVAL redcap=$RVAL tol=2"

echo "REDCAP-SMOKE RESULT=PASS (date=$TODAY local=$LVAL redcap=$RVAL tol=2)" | tee "$OUT"
