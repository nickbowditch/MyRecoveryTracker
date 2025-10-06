#!/bin/bash
set -euo pipefail

TARGET_DIR="evidence/v6.0/distance"
PROBE_FILE="$TARGET_DIR/_probe_GV5.txt"
OUT="$TARGET_DIR/gv5.txt"
mkdir -p "$(dirname "$OUT")" "$TARGET_DIR"

fail() {
  echo "GV5 RESULT=FAIL ($1)" | tee "$OUT"
  echo "--- DEBUG: git root / branch ---" | tee -a "$OUT"
  { git rev-parse --show-toplevel && git rev-parse --abbrev-ref HEAD; } 2>&1 | tee -a "$OUT" || true
  echo "--- DEBUG: .gitignore allow/deny for evidence ---" | tee -a "$OUT"
  { [ -f ".gitignore" ] && { echo "[allow]"; grep -nE '^!evidence(/|$)|^!evidence/v[0-9]+\.[0-9]+(/|$)|^!evidence/v[0-9]+\.[0-9]+/\*\*|^!evidence/v[0-9]+\.[0-9]+/distance(/|$)|^!evidence/v[0-9]+\.[0-9]+/distance/\*\*' .gitignore || true; echo "[deny]"; grep -nE '(^|/)(evidence)(/|$)|\*evidence\*' .gitignore || true; } || echo "<no .gitignore>"; } | tee -a "$OUT"
  echo "--- DEBUG: git check-ignore (verbose) ---" | tee -a "$OUT"
  printf "%s\n" "$CHK_VERBOSE" | tee -a "$OUT"
  echo "--- DEBUG: git add -n output ---" | tee -a "$OUT"
  printf "%s\n" "$ADD_OUT" | tee -a "$OUT"
  echo "--- DEBUG: ls of TARGET_DIR ---" | tee -a "$OUT"
  ls -la "$TARGET_DIR" 2>&1 | tee -a "$OUT" || true
  exit 1
}

git rev-parse --is-inside-work-tree >/dev/null 2>&1 || { echo "GV5 RESULT=FAIL (not inside a git repo)" | tee "$OUT"; exit 2; }

printf "probe %s\n" "$(date +%s)" > "$PROBE_FILE"

CHK_VERBOSE="$(printf "%s\n" "$PROBE_FILE" | git check-ignore -v --stdin 2>/dev/null || true)"
IGN_NONNEG=0
if [ -n "${CHK_VERBOSE// /}" ]; then
  while IFS= read -r line; do
    pat="$(printf "%s" "$line" | sed -E 's/^[^:]+:[0-9]+:([^[:space:]]+)[[:space:]].*$/\1/')"
    if [ -z "$pat" ] || [ "${pat#*!}" = "$pat" ]; then
      IGN_NONNEG=$((IGN_NONNEG+1))
    fi
  done <<< "$CHK_VERBOSE"
fi
[ "$IGN_NONNEG" -eq 0 ] || fail "probe file matched non-negation ignore rule(s)"

ADD_OUT="$(git add -n "$PROBE_FILE" 2>&1 || true)"
{
  echo "GV5 CHECK: Evidence tracked"
  echo "probe_file=$PROBE_FILE"
  echo "--- git add -n (echo) ---"
  printf "%s\n" "$ADD_OUT"
} | tee "$OUT" >/dev/null

echo "$ADD_OUT" | grep -qE "add '.*_probe_GV5.txt'" || fail "git add -n did not indicate addable"

echo "GV5 RESULT=PASS" | tee -a "$OUT"
exit 0
