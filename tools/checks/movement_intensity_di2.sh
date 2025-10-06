#!/bin/bash
set -euo pipefail
PKG="com.nick.myrecoverytracker"
CSV="files/daily_movement_intensity.csv"
OUT="evidence/v6.0/movement_intensity/di2.txt"

mkdir -p "$(dirname "$OUT")"
exec > >(tee "$OUT") 2>&1
fail(){ echo "DI2 RESULT=FAIL ($1)"; exit 1; }

adb get-state >/dev/null 2>&1 || { echo "DI2 RESULT=FAIL (no device)"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "DI2 RESULT=FAIL (app not installed)"; exit 3; }

HDR="$(adb exec-out run-as "$PKG" sed -n '1p' "$CSV" 2>/dev/null | tr -d '\r' || true)"
[ -n "$HDR" ] || fail "CSV missing or unreadable"
[ "$HDR" = "date,intensity" ] || fail "header mismatch: expected 'date,intensity' got '$HDR'"

DUPS="$(adb exec-out run-as "$PKG" awk -F, 'NR>1{c[$1]++} END{for(d in c) if(c[d]>1) print d","c[d]}' "$CSV" 2>/dev/null | tr -d '\r' || true)"
DUP_COUNT="$(printf "%s\n" "${DUPS:-}" | awk 'NF>0{n++} END{print n+0}')"

echo "header=$HDR"
echo "duplicate_dates_count=$DUP_COUNT"
echo "--- DEBUG: duplicate dates with counts (up to 20) ---"
if [ "$DUP_COUNT" -gt 0 ]; then
  printf "%s\n" "$DUPS" | head -n 20
else
  echo "<none>"
fi
echo "--- DEBUG: CSV head ---"
adb exec-out run-as "$PKG" sh -c 'head -n 15 "'"$CSV"'" 2>/dev/null | tr -d "\r"' || true
echo "--- DEBUG: CSV tail ---"
adb exec-out run-as "$PKG" sh -c 'tail -n 15 "'"$CSV"'" 2>/dev/null | tr -d "\r"' || true

[ "$DUP_COUNT" -eq 0 ] || fail "found duplicate date rows"
echo "DI2 RESULT=PASS"
exit 0
