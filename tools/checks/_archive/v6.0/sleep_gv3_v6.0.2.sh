#!/bin/bash
OUT="evidence/v6.0/sleep/gv3.2.txt"
set -e
shopt -s nullglob
SCRIPTS=(tools/checks/sleep_*_v6.0.*.sh)
[ ${#SCRIPTS[@]} -gt 0 ] || { echo "GV-3 RESULT=FAIL (no versioned sleep checks)"; exit 2; }
miss=0
for f in "${SCRIPTS[@]}"; do
  b="$(basename "$f")"
  k="${b#sleep_}"; k="${k%%_v6.0.*.sh}"
  v="$(sed -E 's/^.*_v6\.0\.([0-9]+)\.sh$/\1/' <<<"$b")"
  ev="evidence/v6.0/sleep/${k}.${v}.txt"
  [ -s "$ev" ] || miss=1
done
if [ "$miss" -eq 0 ]; then echo "GV-3 RESULT=PASS" | tee "$OUT"; exit 0; else echo "GV-3 RESULT=FAIL"; exit 1; fi
