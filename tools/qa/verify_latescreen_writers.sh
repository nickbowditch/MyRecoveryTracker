#!/bin/bash
ALLOW_FILE="tools/qa/allow_writers_latescreen.txt"
PATTERN='daily_late_screen\.csv'
FILES=$(git grep -lE "$PATTERN" -- 'app/**' || true)
FILTERED=()
for f in $FILES; do
  if ! grep -E '^\s*//.*daily_late_screen\.csv' "$f" >/dev/null 2>&1; then
    FILTERED+=("$f")
  fi
done
mapfile -t ALLOW < <(grep -v '^\s*#' "$ALLOW_FILE" | sed '/^\s*$/d')
FAILED=0
for f in "${FILTERED[@]}"; do
  ok=0
  for a in "${ALLOW[@]}"; do
    [[ "$f" == "$a" ]] && ok=1 && break
  done
  if [ $ok -eq 0 ]; then
    echo "FAIL: non-whitelisted writer references daily_late_screen.csv → $f"
    FAILED=1
  fi
done
[ $FAILED -eq 0 ] && echo "OK: writer guardrail holds for daily_late_screen.csv"
exit $FAILED
