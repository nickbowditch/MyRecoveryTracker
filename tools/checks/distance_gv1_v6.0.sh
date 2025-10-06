#!/bin/bash
set -euo pipefail

DIR="tools/checks"
OUT="evidence/v6.0/distance/gv1.txt"
mkdir -p "$(dirname "$OUT")"

FILES=($(find "$DIR" -type f -name 'distance_*.sh' 2>/dev/null | sort))
TOTAL=${#FILES[@]}
FAIL=0
TMP="$(mktemp)"

echo "GV1 CHECK: presence and +x on distance scripts" >>"$TMP"
echo "DIRECTORY=$DIR" >>"$TMP"
echo "FOUND=$TOTAL" >>"$TMP"
echo "--- SCRIPT STATUS ---" >>"$TMP"

if [ "$TOTAL" -eq 0 ]; then
  cat "$TMP" | tee "$OUT"
  echo "GV1 RESULT=FAIL (no distance scripts found)" | tee -a "$OUT"
  rm -f "$TMP"
  exit 1
fi

for f in "${FILES[@]}"; do
  if [ -x "$f" ]; then
    echo "OK: $f" >>"$TMP"
  else
    echo "FAIL: $f (not executable)" >>"$TMP"
    FAIL=$((FAIL+1))
  fi
done

echo "--- PERMISSIONS ---" >>"$TMP"
( ls -l "${FILES[@]}" 2>/dev/null || true ) >>"$TMP"

cat "$TMP" | tee "$OUT"
rm -f "$TMP"

if [ "$FAIL" -eq 0 ]; then
  echo "GV1 RESULT=PASS" | tee -a "$OUT"
  exit 0
else
  echo "GV1 RESULT=FAIL ($FAIL non-executable scripts)" | tee -a "$OUT"
  exit 1
fi
