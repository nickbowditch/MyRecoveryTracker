#!/bin/bash
OUT="evidence/v6.0/sleep/gv4.14.txt"
git rev-parse --is-inside-work-tree >/dev/null 2>&1 || { echo "GV-4 RESULT=FAIL (not a git repo)"; exit 2; }
FILES="$(git ls-files -- 'evidence/v6.0/sleep/*.txt' | grep -E '/[a-z0-9_]+\.([0-9]+)\.txt$' || true)"
[ -n "$FILES" ] || { echo "GV-4 RESULT=FAIL (no tracked numbered logs)"; exit 3; }
bad=0; det=""
while IFS= read -r f; do
  [ -z "$f" ] && continue
  grep -q 'RESULT=PASS' "$f" || { bad=1; det="${det}RED:$f"$'\n'; }
done <<< "$FILES"
if [ "$bad" -eq 0 ]; then echo "GV-4 RESULT=PASS" | tee "$OUT"; exit 0; else { echo "GV-4 RESULT=FAIL"; printf "%s" "$det"; } | tee "$OUT"; exit 1; fi
