#!/bin/bash
OUT="evidence/v6.0/sleep/gv3.1.txt"
set -e
shopt -s nullglob
SCRIPTS=(tools/checks/sleep_*_v6.0.*.sh)
[ ${#SCRIPTS[@]} -gt 0 ] || { echo "GV-3 RESULT=FAIL (no versioned sleep checks)"; exit 2; }
miss=0
for f in "${SCRIPTS[@]}"; do
  b="$(basename "$f")"
  check="$(sed -E 's/^sleep_([a-z0-9]+)_v6\.0\.[0-9]+\.sh/\1/' <<<"$b")"
  ver="$(sed -E 's/^sleep_[a-z0-9]+_v6\.0\.([0-9]+)\.sh/\1/' <<<"$b")"
  [ -n "$check" ] && [ -n "$ver" ] || { miss=1; continue; }
  ev="evidence/v6.0/sleep/${check}.${ver}.txt"
  grep -q 'RESULT=PASS' "$ev" 2>/dev/null || miss=1
done
if [ "$miss" -eq 0 ]; then echo "GV-3 RESULT=PASS" | tee "$OUT"; exit 0; else echo "GV-3 RESULT=FAIL"; exit 1; fi
