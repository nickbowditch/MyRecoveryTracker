#!/bin/sh
OUT="evidence/v6.0/sleep/gv1.5.txt"
mkdir -p "$(dirname "$OUT")"
set -- tools/checks/sleep_*_v6.0.*.sh
[ -e "$1" ] || { echo "GV-1 RESULT=FAIL (no sleep_* checks)" | tee "$OUT"; exit 2; }
bad=0
for f in "$@"; do
  bn="$(basename "$f")"
  case "$bn" in sleep_gv*) continue ;; esac
  head -n1 "$f" | grep -qE '^#! */bin/(ba)?sh$' || bad=1
  tail -n +2 "$f" | grep -qE '^[[:space:]]*#' >/dev/null && bad=1
  [ -x "$f" ] || bad=1
done
if [ "$bad" -eq 0 ]; then echo "GV-1 RESULT=PASS" | tee "$OUT"; exit 0; else echo "GV-1 RESULT=FAIL" | tee "$OUT"; exit 1; fi
