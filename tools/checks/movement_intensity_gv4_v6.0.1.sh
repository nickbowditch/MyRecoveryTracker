#!/bin/sh
set -eu
OUT="evidence/v6.0/movement_intensity/gv4.txt"
mkdir -p "$(dirname "$OUT")"

PASS_FILES="$(grep -Rl 'RESULT=PASS' evidence/v6.0/movement_intensity 2>/dev/null || true)"
if [ -n "$PASS_FILES" ]; then
  echo "--- DEBUG: PASS artifacts found ---"
  printf '%s\n' "$PASS_FILES"
  echo "GV4 RESULT=PASS" | tee "$OUT"
  exit 0
fi

echo "--- DEBUG: No PASS artifacts found ---"
find evidence/v6.0/movement_intensity -type f | head -n 20 || true
echo "GV4 RESULT=FAIL (no PASS artifacts)" | tee "$OUT"
exit 1
