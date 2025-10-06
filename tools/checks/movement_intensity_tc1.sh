#!/bin/bash
set -euo pipefail
PKG="com.nick.myrecoverytracker"
CSV="files/daily_movement_intensity.csv"
OUT="evidence/v6.0/movement_intensity/tc1.txt"

mkdir -p "$(dirname "$OUT")"
exec > >(tee "$OUT") 2>&1

fail(){ echo "TC1 RESULT=FAIL ($1)"; exit 1; }

adb get-state >/dev/null 2>&1 || { echo "TC1 RESULT=FAIL (no device)"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "TC1 RESULT=FAIL (app not installed)"; exit 3; }

HDR="$(adb exec-out run-as "$PKG" sed -n '1p' "$CSV" 2>/dev/null | tr -d '\r' || true)"
[ "$HDR" = "date,intensity" ] || fail "header mismatch or file missing"

TMP="$(mktemp)"
adb exec-out run-as "$PKG" sh -c "tail -n +2 '$CSV'" 2>/dev/null | tr -d '\r' > "$TMP" || true
LINES=$(wc -l < "$TMP" | awk '{print $1}')

[ "$LINES" -gt 0 ] || fail "no data rows present"

DUP_DAYS=$(awk -F, 'NR>1{c[$1]++} END{for (d in c) if (c[d]>1) print d}' "$TMP" | wc -l | awk '{print $1}')
if [ "$DUP_DAYS" -gt 0 ]; then
  echo "--- DEBUG: duplicate dates ---"
  awk -F, 'NR>1{c[$1]++} END{for (d in c) if (c[d]>1) print d","c[d]}' "$TMP"
  fail "duplicate day rows detected"
fi

NOW=$(date +%s)
PAST_MIN=$((NOW - 86400*2))
FUTURE_MAX=$((NOW + 86400*2))
BAD_DATES=0

while IFS=, read -r DATE VALUE; do
  [ -z "$DATE" ] && continue
  TS=$(date -j -f "%Y-%m-%d" "$DATE" +%s 2>/dev/null || date -d "$DATE" +%s 2>/dev/null || echo 0)
  if [ "$TS" -lt "$PAST_MIN" ] || [ "$TS" -gt "$FUTURE_MAX" ]; then
    echo "out_of_range_date=$DATE"
    BAD_DATES=1
  fi
done < "$TMP"

rm -f "$TMP"

if [ "$BAD_DATES" -eq 1 ]; then
  fail "one or more dates outside ±1 day tolerance"
fi

echo "TC1 RESULT=PASS"
exit 0
