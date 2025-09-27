#!/bin/sh
set -eu

OUT="evidence/v6.0/unlocks/health.txt"
RUNLOG="evidence/v6.0/unlocks/health.run.txt"
mkdir -p "$(dirname "$OUT")"

scripts="
tools/checks/ee1_v6.0.sh
tools/checks/ee2_v6.0.1.sh
tools/checks/ee3_v6.0.1.sh
tools/checks/tc1_v6.0.3.sh
tools/checks/tc2_v6.0.24.sh
tools/checks/tc5_v6.0.1.sh
tools/checks/unlocks_tc6_v6.0.1.sh
tools/checks/unlocks_di1_v6.0.8.sh
tools/checks/unlocks_di2_v6.0.2.sh
tools/checks/unlocks_di3_v6.0.4.sh
tools/checks/unlocks_di4_v6.0.2.sh
tools/checks/at1_v6.0.5.sh
tools/checks/at2_v6.0.2.sh
tools/checks/at3_v6.0.1.sh
tools/checks/gv1_v6.0.2.sh
tools/checks/gv2_v6.0.14.sh
tools/checks/gv3_v6.0.1.sh
tools/checks/gv4_v6.0.1.sh
tools/checks/gv5_v6.0.1.sh
tools/checks/unlocks_gv6_v6.0.2.sh
"

: >"$RUNLOG"
overall=0

for s in $scripts; do
if [ -x "$s" ]; then
echo "RUN: $s" >>"$RUNLOG"
if out="$("$s" 2>&1)"; then
printf "%s\n" "$out" | grep 'RESULT' | tee -a "$RUNLOG"
else
rc=$?
printf "%s\n" "$out" | grep 'RESULT' || true
echo "FAIL: $s (exit $rc)" | tee -a "$RUNLOG"
overall=1
fi
else
echo "MISSING: $s" | tee -a "$RUNLOG"
overall=1
fi
done

if [ $overall -eq 0 ]; then
echo "UNLOCKS HEALTH=PASS" | tee "$OUT"
exit 0
else
echo "UNLOCKS HEALTH=FAIL" | tee "$OUT"
exit 1
fi
