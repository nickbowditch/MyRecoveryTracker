#!/bin/sh
set -eu

OUT="evidence/v6.0/lnsu/gv4.txt"
mkdir -p "$(dirname "$OUT")"

LOCK="app/locks/work.unique_names.lock"
if [ ! -f "$LOCK" ]; then
echo "GV4 RESULT=FAIL (missing $LOCK)" | tee "$OUT"
exit 1
fi

if grep -q "LateNightRollup" "$LOCK"; then
echo "GV4 RESULT=PASS" | tee "$OUT"
exit 0
fi

echo "GV4 RESULT=FAIL (LateNightRollup not in lock)" | tee "$OUT"
exit 1
