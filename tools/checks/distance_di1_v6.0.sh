#!/bin/bash
set -euo pipefail

PKG="com.nick.myrecoverytracker"
CSV_CANDS=("files/daily_distance_log.csv" "files/daily_distance.csv")
LOCK_CANDS=("files/daily_distance.lock" "files/daily_distance_header.lock" "files/daily_distance.header" "files/daily_distance.lock.txt")
GOLDEN_HDR="date,distance_km"
OUT="evidence/v6.0/distance/di1.txt"
mkdir -p "$(dirname "$OUT")"

fail() {
  echo "DI1 RESULT=FAIL ($1)" | tee "$OUT"
  echo "--- DEBUG: device time ---" | tee -a "$OUT"
  (adb shell 'toybox date "+%F %T %Z %z" 2>/dev/null || date "+%F %T %Z %z"') | tr -d $'\r' | tee -a "$OUT"
  echo "--- DEBUG: app files ---" | tee -a "$OUT"
  adb exec-out run-as "$PKG" sh -c 'pwd; ls -la files' 2>/dev/null | tr -d $'\r' | tee -a "$OUT" || true
  echo "--- DEBUG: CSV head ---" | tee -a "$OUT"
  [ -n "${CSV_PATH:-}" ] && adb exec-out run-as "$PKG" sh -c 'head -n 3 "'"$CSV_PATH"'" | tr -d "\r"' 2>/dev/null | tee -a "$OUT" || echo "<missing>" | tee -a "$OUT"
  echo "--- DEBUG: lock candidates (first line) ---" | tee -a "$OUT"
  for c in "${LOCK_CANDS[@]}"; do
    echo "[$c]" | tee -a "$OUT"
    adb exec-out run-as "$PKG" sh -c '[ -s "'"$c"'" ] && sed -n "1p" "'"$c"'" | tr -d "\r" || echo "<missing>"' 2>/dev/null | tee -a "$OUT"
  done
  echo "--- DEBUG: resolved ---" | tee -a "$OUT"
  echo "CSV_PATH=${CSV_PATH:-<none>}" | tee -a "$OUT"
  echo "LOCK_PATH=${LOCK_PATH:-<none>}" | tee -a "$OUT"
  echo "CSV_HEADER=${CSV_HDR:-<none>}" | tee -a "$OUT"
  echo "LOCK_HEADER=${LOCK_HDR:-<none>}" | tee -a "$OUT"
  exit 1
}

adb get-state >/dev/null 2>&1 || { echo "DI1 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "DI1 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

CSV_PATH=""
for c in "${CSV_CANDS[@]}"; do
  if adb exec-out run-as "$PKG" sh -c '[ -f "'"$c"'" ]' >/dev/null 2>&1; then CSV_PATH="$c"; break; fi
done
[ -n "$CSV_PATH" ] || fail "no daily distance CSV found (checked: ${CSV_CANDS[*]})"

CSV_HDR="$(adb exec-out run-as "$PKG" sh -c 'sed -n "1p" "'"$CSV_PATH"'" 2>/dev/null | tr -d "\r"' || true)"
[ -n "$CSV_HDR" ] || fail "CSV header missing in $CSV_PATH"

LOCK_PATH=""
for c in "${LOCK_CANDS[@]}"; do
  if adb exec-out run-as "$PKG" sh -c '[ -s "'"$c"'" ]' >/dev/null 2>&1; then LOCK_PATH="$c"; break; fi
done

if [ -n "$LOCK_PATH" ]; then
  LOCK_HDR="$(adb exec-out run-as "$PKG" sh -c 'sed -n "1p" "'"$LOCK_PATH"'" 2>/dev/null | tr -d "\r"' || true)"
else
  LOCK_HDR="$GOLDEN_HDR"
fi
[ -n "$LOCK_HDR" ] || LOCK_HDR="$GOLDEN_HDR"

{
  echo "CSV_PATH=$CSV_PATH"
  echo "LOCK_PATH=${LOCK_PATH:-<golden>}"
  echo "CSV_HEADER=$CSV_HDR"
  echo "LOCK_HEADER=$LOCK_HDR"
} | tee "$OUT" >/dev/null

[ "$CSV_HDR" = "$LOCK_HDR" ] || { echo "DI1 RESULT=FAIL (header mismatch) expected='$LOCK_HDR' got='$CSV_HDR'" | tee -a "$OUT"; exit 1; }

echo "DI1 RESULT=PASS" | tee -a "$OUT"
exit 0
