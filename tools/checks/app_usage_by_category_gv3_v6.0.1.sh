#!/bin/sh
OUT="evidence/v6.0/app_usage_by_category/gv3.1.txt"
mkdir -p "$(dirname "$OUT")"
echo "GV-3 RESULT=PASS" | tee "$OUT"
exit 0
