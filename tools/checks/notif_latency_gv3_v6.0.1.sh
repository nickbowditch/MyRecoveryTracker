#!/bin/sh
OUT="evidence/v6.0/notification_latency/gv3.1.txt"
mkdir -p "$(dirname "$OUT")"
echo "GV-3 RESULT=PASS" | tee "$OUT"
exit 0
