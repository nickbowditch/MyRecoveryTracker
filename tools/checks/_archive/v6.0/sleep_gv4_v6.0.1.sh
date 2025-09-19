#!/bin/bash
OUT="evidence/v6.0/sleep/gv4.1.txt"
git rev-parse --is-inside-work-tree >/dev/null 2>&1 || { echo "GV-4 RESULT=FAIL (not a git repo)"; exit 2; }
shopt -s nullglob
PASS_FILES=(evidence/v6.0/sleep/*.txt)
[ ${#PASS_FILES[@]} -gt 0 ] || { echo "GV-4 RESULT=FAIL (no evidence logs)"; exit 3; }
bad=0
for f in "${PASS_FILES[@]}"; do
  grep -q 'RESULT=PASS' "$f" || { bad=1; }
  git ls-files --error-unmatch "$f" >/dev/null 2>&1 || { bad=1; }
done
if [ "$bad" -eq 0 ]; then echo "GV-4 RESULT=PASS" | tee "$OUT"; exit 0; else echo "GV-4 RESULT=FAIL"; exit 1; fi
