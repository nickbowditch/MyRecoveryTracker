#!/bin/sh
set -eu

OUT="evidence/v6.0/lnsu/gv5.txt"
mkdir -p "$(dirname "$OUT")"

LOCK="app/locks/work.unique_names.lock"
[ -f "$LOCK" ] || { echo "GV5 RESULT=FAIL (missing $LOCK)" | tee "$OUT"; exit 1; }

req_once="once-LateNightRollup"
req_once_policy="REPLACE"

req_periodic="periodic_late_night_rollup"
req_periodic_policy="UPDATE"

ok=1

line_once=$(grep -E "^[[:space:]]*$req_once[[:space:]]*,[[:space:]]*([A-Z_\.]+)[[:space:]]*$" "$LOCK" || true)
if [ -z "$line_once" ]; then
  echo "GV5 RESULT=FAIL (missing $req_once in lock)" | tee "$OUT"
  exit 1
fi
pol_once=$(printf "%s\n" "$line_once" | sed -E "s/^.*,[[:space:]]*//")
pol_once=$(printf "%s" "$pol_once" | sed 's/ExistingWorkPolicy\.//')
[ "$pol_once" = "$req_once_policy" ] || ok=0

line_per=$(grep -E "^[[:space:]]*$req_periodic[[:space:]]*,[[:space:]]*([A-Z_\.]+)[[:space:]]*$" "$LOCK" || true)
if [ -n "$line_per" ]; then
  pol_per=$(printf "%s\n" "$line_per" | sed -E "s/^.*,[[:space:]]*//")
  pol_per=$(printf "%s" "$pol_per" | sed 's/ExistingPeriodicWorkPolicy\.//')
  [ "$pol_per" = "$req_periodic_policy" ] || ok=0
fi

if [ "$ok" -eq 1 ]; then
  echo "GV5 RESULT=PASS" | tee "$OUT"
  exit 0
fi

echo "GV5 RESULT=FAIL (policy mismatch)" | tee "$OUT"
exit 1
