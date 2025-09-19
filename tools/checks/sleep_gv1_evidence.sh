#!/usr/bin/env bash
set -euo pipefail
FILE="schemas/rollups.json"
TARGET_FILE="daily_sleep_summary.csv"
EXP_HEADER='["date","sleep_time","wake_time","duration_hours"]'

[ -f "$FILE" ] || { echo "Sleep GV-1 RESULT=FAIL (missing $FILE)"; exit 1; }

entry_json="$(
  jq -cr --arg tf "$TARGET_FILE" '
    if has($tf) then .[$tf]
    else (to_entries[]? | select(.value.file==$tf) | .value) // {}
    end
  ' "$FILE"
)"

[ -n "$entry_json" ] && [ "$entry_json" != "{}" ] || { echo "Sleep GV-1 RESULT=FAIL (no entry for $TARGET_FILE)"; exit 1; }

hdr_ok=$(echo "$entry_json" | jq -e --argjson exp "$EXP_HEADER" '.header == $exp' >/dev/null 2>&1; echo $?)
ver_ok=$(echo "$entry_json" | jq -e 'has("schema_version")' >/dev/null 2>&1; echo $?)

echo "---- schemas/rollups.json entry for $TARGET_FILE ----"
echo "$entry_json" | jq .
echo "-----------------------------------------------------"

if [ "$hdr_ok" -eq 0 ] && [ "$ver_ok" -eq 0 ]; then
  echo "Sleep GV-1 RESULT=PASS"
  exit 0
else
  echo "Sleep GV-1 RESULT=FAIL"
  exit 1
fi
