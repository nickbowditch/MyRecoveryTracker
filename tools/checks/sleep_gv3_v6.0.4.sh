#!/bin/bash
OUT="evidence/v6.0/sleep/gv3.4.txt"

me="$(basename "$0")"

shopt -s nullglob
SCRIPTS=(tools/checks/sleep_*_v6.0.*.sh)
[ ${#SCRIPTS[@]} -gt 0 ] || { echo "GV-3 RESULT=FAIL (no versioned sleep checks)" | tee "$OUT"; exit 2; }

miss=0; det=""
for f in "${SCRIPTS[@]}"; do
  b="$(basename "$f")"
  [ "$b" = "$me" ] && continue
  s="${b%.sh}"
  k="${s#sleep_}"; k="${k%%_v6.0.*}"
  v="${s##*_v6.0.}"
  ev="evidence/v6.0/sleep/${k}.${v}.txt"
  if [ ! -s "$ev" ]; then miss=1; det="${det}MISSING:$ev"$'\n'; continue; fi
  grep -q 'RESULT=PASS' "$ev" || { miss=1; det="${det}RED:$ev"$'\n'; }
done

if [ "$miss" -eq 0 ]; then
  echo "GV-3 RESULT=PASS" | tee "$OUT"; exit 0
else
  { echo "GV-3 RESULT=FAIL"; printf "%s" "$det"; } | tee "$OUT"; exit 1
fi
