#!/usr/bin/env bash
set -euo pipefail

FILE="schemas/rollups.json"
TARGET_FILE="daily_sleep_summary.csv"
EXP_HEADER='["date","sleep_time","wake_time","duration_hours"]'

[ -f "$FILE" ] || { echo "Sleep GV-1 RESULT=FAIL (missing $FILE)"; exit 1; }

entry_json="$(
  jq -cr --arg tf "$TARGET_FILE" '
    if has($tf) then .[$tf]
    else
      (to_entries[]? | select(.value.file==$tf) | .value) // {}
    end
  ' "$FILE"
)"

[ -n "$entry_json" ] && [ "$entry_json" != "{}" ] || { echo "Sleep GV-1 RESULT=FAIL (no entry for $TARGET_FILE)"; exit 1; }

echo "$entry_json" | jq -e --argjson exp "$EXP_HEADER" '.header == $exp' >/dev/null \
  || { echo "Sleep GV-1 RESULT=FAIL (header mismatch)"; exit 1; }

echo "$entry_json" | jq -e 'has("schema_version")' >/dev/null \
  || { echo "Sleep GV-1 RESULT=FAIL (missing schema_version)"; exit 1; }

echo "Sleep GV-1 RESULT=PASS"
