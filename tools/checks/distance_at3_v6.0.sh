#!/bin/bash
set -euo pipefail

TARGET_DIR="evidence/v6.0/distance"
PROBE_FILE="$TARGET_DIR/_probe_AT3.txt"
OUT="$TARGET_DIR/at3.txt"
mkdir -p "$(dirname "$OUT")" "$TARGET_DIR"

fail() {
  echo "AT3 RESULT=FAIL ($1)" | tee "$OUT"
  echo "--- DEBUG: git root / branch ---" | tee -a "$OUT"
  { git rev-parse --show-toplevel && git rev-parse --abbrev-ref HEAD; } 2>&1 | tee -a "$OUT" || true
  echo "--- DEBUG: .gitignore allow/deny for evidence ---" | tee -a "$OUT"
  { [ -f ".gitignore" ] && { echo "[allow]"; grep -nE '^!evidence(/|$)|^!evidence/v6\.0(/|$)|^!evidence/v6\.0/\*\*|^!evidence/v6\.0/distance(/|$)' .gitignore || true; echo "[deny]"; grep -nE '(^|/)(evidence)(/|$)|\*evidence\*' .gitignore || true; } || echo "<no .gitignore>"; } | tee -a "$OUT"
  echo "--- DEBUG: git check-ignore (verbose) ---" | tee -a "$OUT"
  printf "%s\n" "$PROBE_FILE" | git check-ignore -v --stdin 2>&1 | tee -a "$OUT" || true
  echo "--- DEBUG: git add -n output ---" | tee -a "$OUT"
  git add -n "$PROBE_FILE" 2>&1 | tee -a "$OUT" || true
  echo "--- DEBUG: ls of TARGET_DIR ---" | tee -a "$OUT"
  ls -la "$TARGET_DIR" 2>&1 | tee -a "$OUT" || true
  exit 1
}

git rev-parse --is-inside-work-tree >/dev/null 2>&1 || { echo "AT3 RESULT=FAIL (not inside a git repo)" | tee "$OUT"; exit 2; }

printf "probe %s\n" "$(date +%s)" > "$PROBE_FILE"

IGN_VERBOSE="$(printf "%s\n" "$PROBE_FILE" | git check-ignore -v --stdin 2>/dev/null || true)"
if [ -n "${IGN_VERBOSE// /}" ]; then
  bad=0
  while IFS= read -r line; do
    pat="$(printf "%s" "$line" | sed -E 's/^[^:]+:[0-9]+:([^ \t]+)[ \t].*$/\1/')"
    case "$pat" in
      \!*) : ;;  
      *) bad=1 ;;
    esac
  done <<< "$IGN_VERBOSE"
  [ "$bad" -eq 0 ] || fail "probe file is ignored by git"
fi

ADD_OUT="$(git add -n "$PROBE_FILE" 2>&1 || true)"
echo "$ADD_OUT" | grep -q "add '.*_probe_AT3.txt'" || fail "git add -n did not indicate addable"

{
  echo "probe_file=$PROBE_FILE"
  echo "--- git check-ignore (verbose) ---"
  printf "%s\n" "${IGN_VERBOSE:-<empty>}"
  echo "--- git add -n (echo) ---"
  printf "%s\n" "$ADD_OUT"
} | tee "$OUT" >/dev/null

echo "AT3 RESULT=PASS" | tee -a "$OUT"
exit 0
