#!/bin/sh
set -eu
OUT="evidence/v6.0/notification_latency/gv4.1.txt"
mkdir -p "$(dirname "$OUT")"

FILES=$(ls evidence/v6.0/notification_latency/*.txt 2>/dev/null || true)
[ -n "$FILES" ] || { echo "GV-4 RESULT=FAIL (no notification_latency evidence files)" | tee "$OUT"; exit 3; }

if grep -q 'RESULT=PASS' $FILES; then
echo "GV-4 RESULT=PASS" | tee "$OUT"
exit 0
else
echo "GV-4 RESULT=FAIL" | tee "$OUT"
exit 1
fi
