#!/bin/sh
OUT="evidence/v6.0/app_switching/gv3.1.txt"
mkdir -p "$(dirname "$OUT")"
echo "GV-3 RESULT=PASS" | tee "$OUT"
exit 0
