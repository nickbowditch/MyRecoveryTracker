#!/bin/bash
set -euo pipefail
PKG="com.nick.myrecoverytracker"
CSV="files/daily_movement_intensity.csv"
OUT="evidence/v6.0/movement_intensity/tc5.txt"

mkdir -p "$(dirname "$OUT")"
exec > >(tee "$OUT") 2>&1
fail(){ echo "TC5 RESULT=FAIL ($1)"; exit 1; }

adb get-state >/dev/null 2>&1 || { echo "TC5 RESULT=FAIL (no device)"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "TC5 RESULT=FAIL (app not installed)"; exit 3; }

HDR="$(adb exec-out run-as "$PKG" sed -n '1p' "$CSV" 2>/dev/null | tr -d '\r' || true)"
[ "$HDR" = "date,intensity" ] || fail "daily CSV header mismatch or missing"

TMP="$(mktemp)"
adb exec-out run-as "$PKG" cat "$CSV" 2>/dev/null | tr -d '\r' > "$TMP" || true
[ -s "$TMP" ] || fail "CSV missing or empty"

TODAY="$(adb shell 'toybox date +%F 2>/dev/null || date +%F' | tr -d $'\r')"
[ -n "$TODAY" ] || fail "date read error"

dates_file="$(mktemp)"
awk -F, 'NR>1 && $1 ~ /^[0-9]{4}-[0-9]{2}-[0-9]{2}$/ {print $1}' "$TMP" | sort -u > "$dates_file"
[ -s "$dates_file" ] || fail "no valid date rows"

to_epoch() {
  local d="$1" e=""
  e="$(date -j -f '%Y-%m-%d' "$d" +%s 2>/dev/null || date -d "$d" +%s 2>/dev/null || true)"
  if [ -z "$e" ]; then
    echo ""
  else
    echo "$e"
  fi
}

max_gap=0
violations_file="$(mktemp)"
prev=""
while IFS= read -r d; do
  if [ -n "$prev" ]; then
    ep_prev="$(to_epoch "$prev")"
    ep_curr="$(to_epoch "$d")"
    if [ -z "$ep_prev" ] || [ -z "$ep_curr" ]; then
      echo "--- DEBUG: epoch conversion failed for prev=$prev curr=$d ---"
      rm -f "$TMP" "$dates_file" "$violations_file"
      fail "cannot convert dates to epoch seconds"
    fi
    diff_days=$(( (ep_curr - ep_prev) / 86400 ))
    if [ "$diff_days" -gt "$max_gap" ]; then max_gap="$diff_days"; fi
    if [ "$diff_days" -gt 2 ]; then
      echo "$prev -> $d : gap=${diff_days}d" >> "$violations_file"
    fi
  fi
  prev="$d"
done < "$dates_file"

violations_count=0
[ -s "$violations_file" ] && violations_count="$(wc -l < "$violations_file" | tr -d '[:space:]')" || violations_count=0

echo "today=$TODAY"
echo "header=$HDR"
echo "--- DEBUG: CSV head ---"
head -n 10 "$TMP"
echo "--- DEBUG: distinct dates (count=$(wc -l < "$dates_file" | tr -d '[:space:]')) ---"
head -n 30 "$dates_file"
echo "--- DEBUG: max_gap_days=$max_gap ---"
echo "--- DEBUG: gaps >2 days (count=$violations_count) ---"
[ -s "$violations_file" ] && head -n 50 "$violations_file" || echo "<none>"

rm -f "$TMP" "$dates_file" "$violations_file"

if [ "$violations_count" -gt 0 ]; then
  fail "found $violations_count gaps exceeding 2 days (max_gap=${max_gap}d)"
fi

echo "TC5 RESULT=PASS"
exit 0
