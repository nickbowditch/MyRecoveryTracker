#!/bin/bash
need=("!evidence/" "!evidence/v6.0/" "!evidence/v6.0/" "!evidence/v6.0//*.txt")
for l in "${need[@]}"; do
grep -Fxq "$l" .gitignore || { echo "GV-5 RESULT=FAIL (missing $l in .gitignore)"; exit 1; }
done
echo "GV-5 RESULT=PASS"
exit 0
