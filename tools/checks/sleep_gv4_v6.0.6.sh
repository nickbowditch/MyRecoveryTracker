#!/bin/bash
OUT="evidence/v6.0/sleep/gv4.6.txt"
git rev-parse --is-inside-work-tree >/dev/null 2>&1 || { echo "GV-4 RESULT=FAIL (not a git repo)" | tee "$OUT"; exit 2; }
FILES="$(find evidence/v6.0/sleep -maxdepth 1 -type f -name '*.txt' 2>/dev/null | grep -E '/[a-z0-9_]+\.([0-9]+)\.txt$' || true)"
[ -n "$FILES" ] || { echo "GV-4 RESULT=FAIL (no numbered PASS logs)" | tee "$OUT"; exit 3; }
bad=0; det=""
while IFS= read -r f; do
  [ -z "$f" ] && continue
  grep -q 'RESULT=PASS' "$f" || { bad=1; det="${det}RED:$f"$'\n'; continue; }
  git ls-files --error-unmatch "$f" >/dev/null 2>&1 || { bad=1; det="${det}UNTRACKED:$f"$'\n'; }
done <<< "$FILES"
if [ "$bad" -eq 0 ]; then echo "GV-4 RESULT=PASS" | tee "$OUT"; exit 0; else { echo "GV-4 RESULT=FAIL"; printf "%s" "$det"; } | tee "$OUT"; exit 1; fi
