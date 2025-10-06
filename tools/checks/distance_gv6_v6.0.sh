#!/bin/bash
set -euo pipefail

PKG="com.nick.myrecoverytracker"
CSV_CANDS=("files/daily_distance_log.csv" "files/daily_distance.csv")
LOCK_CANDS=("app/locks/daily_distance_log.header" "app/locks/daily_distance.header")
OUT="evidence/v6.0/distance/gv6.txt"
mkdir -p "$(dirname "$OUT")" app/locks

fail(){
  echo "GV6 RESULT=FAIL ($1)" | tee "$OUT"
  echo "--- DEBUG: repo locks (first line) ---" | tee -a "$OUT"
  for l in "${LOCK_CANDS[@]}"; do
    echo "[$l]" | tee -a "$OUT"
    { [ -f "$l" ] && head -n 1 "$l" || echo "<missing>"; } | tr -d $'\r' | tee -a "$OUT"
  done
  echo "--- DEBUG: device CSV candidates (first line) ---" | tee -a "$OUT"
  for c in "${CSV_CANDS[@]}"; do
    echo "[$c]" | tee -a "$OUT"
    adb exec-out run-as "$PKG" sh -c 'sed -n "1p" "'"$c"'" 2>/dev/null | tr -d "\r" || echo "<missing>"' 2>/dev/null | tee -a "$OUT"
  done
  echo "--- DEBUG: resolved ---" | tee -a "$OUT"
  echo "LOCK_PATH=${LOCK_PATH:-<none>}" | tee -a "$OUT"
  echo "CSV_PATH=${CSV_PATH:-<none>}" | tee -a "$OUT"
  echo "LOCK_HEADER=${LOCK_HDR:-<none>}" | tee -a "$OUT"
  echo "CSV_HEADER=${CSV_HDR:-<none>}" | tee -a "$OUT"
  exit 1
}

adb get-state >/dev/null 2>&1 || { echo "GV6 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "GV6 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

CSV_PATH=""
CSV_HDR=""
for c in "${CSV_CANDS[@]}"; do
  H="$(adb exec-out run-as "$PKG" sh -c 'sed -n "1p" "'"$c"'" 2>/dev/null | tr -d "\r"' 2>/dev/null || true)"
  if [ -n "$H" ]; then CSV_PATH="$c"; CSV_HDR="$H"; break; fi
done
[ -n "$CSV_PATH" ] || fail "no readable daily distance CSV on device"

LOCK_PATH=""
for l in "${LOCK_CANDS[@]}"; do
  if [ -f "$l" ]; then LOCK_PATH="$l"; break; fi
done

if [ -z "$LOCK_PATH" ]; then
  LOCK_PATH="${LOCK_CANDS[0]}"
  printf "%s\n" "$CSV_HDR" > "$LOCK_PATH"
fi

LOCK_HDR="$(sed -n '1p' "$LOCK_PATH" 2>/dev/null | tr -d $'\r' || true)"
[ -n "$LOCK_HDR" ] || fail "empty lock header"

{
  echo "lock_repo_path=$LOCK_PATH"
  echo "csv_device_path=$CSV_PATH"
  echo "lock_header=$LOCK_HDR"
  echo "csv_header=$CSV_HDR"
} | tee "$OUT" >/dev/null

[ "$CSV_HDR" = "$LOCK_HDR" ] || fail "header mismatch: repo lock vs device CSV"

echo "GV6 RESULT=PASS" | tee -a "$OUT"
exit 0
