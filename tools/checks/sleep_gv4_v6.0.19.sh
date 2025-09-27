#!/bin/sh
set -eu
OUT="evidence/v6.0/sleep/gv4.19.txt"
mkdir -p "$(dirname "$OUT")"

FILES=$(ls evidence/v6.0/sleep/*.txt 2>/dev/null || true)
[ -n "$FILES" ] || { echo "GV-4 RESULT=FAIL (no sleep evidence files)" | tee "$OUT"; exit 3; }

if grep -q 'RESULT=PASS' $FILES; then
echo "GV-4 RESULT=PASS" | tee "$OUT"
exit 0
else
echo "GV-4 RESULT=FAIL" | tee "$OUT"
exit 1
fi
