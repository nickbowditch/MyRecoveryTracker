#!/bin/bash
set -euo pipefail

PKG="com.nick.myrecoverytracker"
CSV_CANDS=("files/daily_distance_log.csv" "files/daily_distance.csv")
GOLDEN_HDR="date,distance_km"
OUT="evidence/v6.0/distance/di2.txt"
mkdir -p "$(dirname "$OUT")"

fail() {
  echo "DI2 RESULT=FAIL ($1)" | tee "$OUT"
  echo "--- DEBUG: device date/time (local) ---" | tee -a "$OUT"
  (adb shell 'toybox date "+%F %T %Z %z" 2>/dev/null || date "+%F %T %Z %z"') | tr -d $'\r' | tee -a "$OUT"
  echo "--- DEBUG: app files dir (pwd; ls -la) ---" | tee -a "$OUT"
  adb exec-out run-as "$PKG" sh -c 'pwd; ls -la' 2>/dev/null | tr -d $'\r' | tee -a "$OUT" || true
  echo "--- DEBUG: CSV exists/size ---" | tee -a "$OUT"
  [ -n "${CSV_PATH:-}" ] && adb exec-out run-as "$PKG" sh -c '[ -f "'"$CSV_PATH"'" ] && { ls -l "'"$CSV_PATH"'"; wc -l "'"$CSV_PATH"'"; } || echo "<missing>"' 2>/dev/null | tr -d $'\r' | tee -a "$OUT" || true
  echo "--- DEBUG: CSV head ---" | tee -a "$OUT"
  [ -n "${CSV_PATH:-}" ] && adb exec-out run-as "$PKG" sh -c 'head -n 20 "'"$CSV_PATH"'" | tr -d "\r"' 2>/dev/null | tee -a "$OUT" || true
  echo "--- DEBUG: CSV tail ---" | tee -a "$OUT"
  [ -n "${CSV_PATH:-}" ] && adb exec-out run-as "$PKG" sh -c 'tail -n 20 "'"$CSV_PATH"'" | tr -d "\r"' 2>/dev/null | tee -a "$OUT" || true
  echo "--- DEBUG: duplicates (date,count) ---" | tee -a "$OUT"
  printf "%s\n" "${DUPS:-<none>}" | tee -a "$OUT"
  exit 1
}

adb get-state >/dev/null 2>&1 || { echo "DI2 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "DI2 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

CSV_PATH=""
for c in "${CSV_CANDS[@]}"; do
  if adb exec-out run-as "$PKG" sh -c '[ -f "'"$c"'" ]' >/dev/null 2>&1; then CSV_PATH="$c"; break; fi
done
[ -n "$CSV_PATH" ] || fail "no daily distance CSV found (checked: ${CSV_CANDS[*]})"

HDR="$(adb exec-out run-as "$PKG" sh -c 'sed -n "1p" "'"$CSV_PATH"'" 2>/dev/null | tr -d "\r"' || true)"
[ "$HDR" = "$GOLDEN_HDR" ] || fail "bad header (want: $GOLDEN_HDR; got: ${HDR:-<none>})"

DUPS="$(adb exec-out run-as "$PKG" awk -F, 'NR>1 && $1~/^[0-9]{4}-[0-9]{2}-[0-9]{2}$/ {c[$1]++} END{for (d in c) if (c[d]>1) printf "%s,%d\n",d,c[d]}' "$CSV_PATH" 2>/dev/null | tr -d $'\r' || true)"

{
  echo "CSV=$CSV_PATH"
  echo "HEADER=$HDR"
  echo "DUPLICATES_COUNT=$(printf "%s\n" "${DUPS:-}" | sed '/^$/d' | wc -l | tr -d ' ')"
  echo "--- DUPLICATES (first 50) ---"
  printf "%s\n" "${DUPS:-<none>}" | sed -n '1,50p'
} | tee "$OUT" >/dev/null

[ -z "${DUPS// /}" ] || { echo "DI2 RESULT=FAIL (duplicate date rows found)" | tee -a "$OUT"; exit 1; }

echo "DI2 RESULT=PASS" | tee -a "$OUT"
exit 0
