#!/bin/bash
find evidence/v6.0 -type f -name '*.txt' | grep -q . || { echo "GV-4 RESULT=FAIL (no evidence logs)"; exit 1; }
echo "GV-4 RESULT=PASS"
exit 0
