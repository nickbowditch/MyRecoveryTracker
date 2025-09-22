#!/bin/sh
OUT="evidence/v6.0/sleep/gv1.7.txt"
mkdir -p "$(dirname "$OUT")"
for d in tools/checks tools/fixtures tools/env evidence/v6.0 app/locks; do
[ -d "$d" ] || { echo "GV-1 RESULT=FAIL (missing dir $d)" | tee "$OUT"; exit 1; }
done
echo "GV-1 RESULT=PASS" | tee "$OUT"
exit 0
