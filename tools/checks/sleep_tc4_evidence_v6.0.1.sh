#!/bin/bash
OUT="evidence/v6.0/sleep/tc4.6.txt"
tools/checks/sleep_tc4_v6.0.6.sh | tee evidence/v6.0/sleep/tc4.console.6.txt
s=$?
if [ $s -eq 0 ]; then
echo "TC-4 RESULT=PASS" > "$OUT"; exit 0
else
echo "TC-4 RESULT=FAIL" > "$OUT"; exit 1
fi
