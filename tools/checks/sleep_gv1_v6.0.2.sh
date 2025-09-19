#!/bin/bash
OUT="evidence/v6.0/sleep/gv1.2.txt"
set -e
shopt -s nullglob
SCRIPTS=(tools/checks/sleep_*_v6.0*.sh)
[ ${#SCRIPTS[@]} -gt 0 ] || { echo "GV-1 RESULT=FAIL (no sleep_* checks)"; exit 2; }

bad=0
for f in "${SCRIPTS[@]}"; do
  head -n1 "$f" | grep -qE '^#! */bin/(ba|z)?sh' || bad=1
  tail -n +2 "$f" | grep -qE '^[[:space:]]*#' && bad=1
done

if [ "$bad" -eq 0 ]; then echo "GV-1 RESULT=PASS" | tee "$OUT"; exit 0; else echo "GV-1 RESULT=FAIL"; exit 1; fi
