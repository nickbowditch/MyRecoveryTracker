#!/bin/sh
OUT="evidence/v6.0/sleep/gv4.18.txt"
mkdir -p "$(dirname "$OUT")"
FILES="$(git ls-files -- 'evidence/v6.0/sleep/*.txt' 2>/dev/null || true)"
[ -n "$FILES" ] || { echo "GV-4 RESULT=FAIL (no tracked sleep evidence)" | tee "$OUT"; exit 3; }
PASS_ANY="$(grep -l 'RESULT=PASS' $FILES 2>/dev/null | head -n1)"
[ -n "$PASS_ANY" ] || { echo "GV-4 RESULT=FAIL" | tee "$OUT"; exit 1; }
echo "GV-4 RESULT=PASS" | tee "$OUT"
exit 0
