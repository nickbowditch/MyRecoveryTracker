#!/bin/bash
OUT="evidence/v6.0/sleep/gv4.3.txt"

git rev-parse --is-inside-work-tree >/dev/null 2>&1 || { echo "GV-4 RESULT=FAIL (not a git repo)" | tee "$OUT"; exit 2; }

shopt -s nullglob
FILES=(evidence/v6.0/sleep/*.txt)
[ ${#FILES[@]} -gt 0 ] || { echo "GV-4 RESULT=FAIL (no evidence logs)" | tee "$OUT"; exit 3; }

bad=0; det=""
for f in "${FILES[@]}"; do
  if ! grep -q 'RESULT=PASS' "$f"; then
    bad=1; det="${det}RED:$f"$'\n'
    continue
  fi
  if ! git ls-files --error-unmatch "$f" >/dev/null 2>&1; then
    bad=1; det="${det}UNTRACKED:$f"$'\n'
  fi
done

if [ "$bad" -eq 0 ]; then
  echo "GV-4 RESULT=PASS" | tee "$OUT"; exit 0
else
  { echo "GV-4 RESULT=FAIL"; printf "%s" "$det"; } | tee "$OUT"; exit 1
fi
