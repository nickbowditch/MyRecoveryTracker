#!/bin/bash
OUT="evidence/v6.0/unlocks/at1.2.txt"
tools/checks/at1_v6.0.2.sh | tee evidence/v6.0/unlocks/at1.console.2.txt
s=$?
if [ $s -eq 0 ]; then
echo "AT-1 RESULT=PASS" > "$OUT"; exit 0
else
echo "AT-1 RESULT=FAIL" > "$OUT"; exit 1
fi
