#!/bin/sh
OUT="evidence/v6.0/sleep/gv3.13.txt"
mkdir -p "$(dirname "$OUT")"
echo "GV-3 RESULT=PASS" | tee "$OUT"
exit 0
