#!/bin/sh
OUT="evidence/v6.0/sleep/gv3.10.txt"
mkdir -p "$(dirname "$OUT")"
set -- tools/checks/sleep_*_v6.0.*.sh
[ -e "$1" ] || { echo "GV-3 RESULT=FAIL (no versioned sleep checks)" | tee "$OUT"; exit 2; }
miss=0; det=""
for f in "$@"; do
  bn="$(basename "$f")"
  case "$bn" in sleep_gv*) continue ;; esac
  b="${bn%.sh}"
  k="${b#sleep_}"; k="${k%%_v6.0.*}"
  v="${b##*_v6.0.}"
  ev="evidence/v6.0/sleep/${k}.${v}.txt"
  [ -s "$ev" ] || { miss=1; det="${det}MISSING:${ev}\n"; continue; }
  grep -q 'RESULT=PASS' "$ev" || { miss=1; det="${det}RED:${ev}\n"; }
done
if [ "$miss" -eq 0 ]; then echo "GV-3 RESULT=PASS" | tee "$OUT"; exit 0; else { echo "GV-3 RESULT=FAIL"; printf "%b" "$det"; } | tee "$OUT"; exit 1; fi
