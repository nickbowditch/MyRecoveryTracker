#!/bin/sh
set -eu

OUT="evidence/v6.0/sleep/health.txt"
RUNLOG="evidence/v6.0/sleep/health.run.txt"
mkdir -p "$(dirname "$OUT")"

scripts="
tools/checks/ee1_sleep_v6.0.sh
tools/checks/ee2_sleep_v6.0.2.sh
tools/checks/ee3_sleep_v6.0.2.sh
tools/checks/tc1_sleep_v6.0.6.sh
tools/checks/tc3_sleep_v6.0.1.sh
tools/checks/tc5_sleep_v6.0.1.sh
tools/checks/tc6_sleep_v6.0.1.sh
tools/checks/sleep_di1_v6.0.4.sh
tools/checks/sleep_di2_v6.0.2.sh
tools/checks/sleep_di3_v6.0.14.sh
tools/checks/sleep_di4_v6.0.2.sh
tools/checks/at1_sleep_v6.0.3.sh
tools/checks/at2_sleep_v6.0.2.sh
tools/checks/at3_sleep_v6.0.1.sh
tools/checks/sleep_gv1_v6.0.7.sh
tools/checks/sleep_gv2_v6.0.5.sh
tools/checks/sleep_gv3_v6.0.13.sh
tools/checks/sleep_gv4_v6.0.18.sh
tools/checks/sleep_gv5_v6.0.1.sh
tools/checks/sleep_gv6_v6.0.1.sh
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
  echo "SLEEP HEALTH=PASS" | tee "$OUT"
  exit 0
else
  echo "SLEEP HEALTH=FAIL" | tee "$OUT"
  exit 1
fi
