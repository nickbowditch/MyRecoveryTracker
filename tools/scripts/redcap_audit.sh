#!/usr/bin/env bash
set -euo pipefail

URL="https://redcap.une.edu.au/redcap_v15.5.0/API/project_api.php?pid=59"
TOKEN="B717098DF6799DBE0406F0DE0080CDA8"

DAYS="${REDCAP_DAYS:-7}"
FILTER_LOGIC="${REDCAP_FILTER_LOGIC:-}"
REPORT_ID="${REDCAP_REPORT_ID:-}"

OUT="evidence/redcap_audit_$(date -u +%Y%m%dT%H%M%SZ)"
mkdir -p "$OUT"

BEGIN="$(date -u -v-"$DAYS"d '+%Y-%m-%d %H:%M:%S')"
END="$(date -u '+%Y-%m-%d %H:%M:%S')"

curl -sS -X POST \
  -d "token=$TOKEN" \
  -d "content=version" \
  -d "format=json" \
  "$URL" > "$OUT/version.txt"

curl -sS -X POST \
  -d "token=$TOKEN" \
  -d "content=log" \
  -d "format=json" \
  -d "beginTime=$BEGIN" \
  -d "endTime=$END" \
  "$URL" > "$OUT/logging.json"

if [ -n "$FILTER_LOGIC" ]; then
  curl -sS -X POST \
    -d "token=$TOKEN" \
    -d "content=record" \
    -d "format=json" \
    -d "type=flat" \
    -d "rawOrLabel=raw" \
    -d "exportDataAccessGroups=true" \
    --data-urlencode "filterLogic=$FILTER_LOGIC" \
    "$URL" > "$OUT/records.json"
else
  curl -sS -X POST \
    -d "token=$TOKEN" \
    -d "content=record" \
    -d "format=json" \
    -d "type=flat" \
    -d "rawOrLabel=raw" \
    -d "exportDataAccessGroups=true" \
    "$URL" > "$OUT/records.json"
fi

if [ -n "$REPORT_ID" ]; then
  curl -sS -X POST \
    -d "token=$TOKEN" \
    -d "content=report" \
    -d "format=json" \
    -d "report_id=$REPORT_ID" \
    "$URL" > "$OUT/report_${REPORT_ID}.json" || true
fi

if command -v python3 >/dev/null 2>&1; then
  python3 - "$OUT" <<'PY'
import json, os, sys
out = sys.argv[1]
def count(p):
    try:
        with open(p,'rb') as f: return len(json.load(f))
    except Exception:
        return 'NA'
print(f"logs={count(os.path.join(out,'logging.json'))}")
print(f"records={count(os.path.join(out,'records.json'))}")
PY
else
  {
    echo "logs=NA"
    echo "records=NA"
  }
fi > "$OUT/_counts.txt"

echo "$OUT"
