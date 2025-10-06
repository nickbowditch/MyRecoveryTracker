#!/bin/bash
set -euo pipefail

TARGET_DIR="evidence/v6.0/distance"
OUT="evidence/v6.0/distance/di4.txt"
mkdir -p "$(dirname "$OUT")"

fail() {
  echo "DI4 RESULT=FAIL ($1)" | tee "$OUT"
  echo "--- DEBUG: git root / branch ---" | tee -a "$OUT"
  { git rev-parse --show-toplevel && git rev-parse --abbrev-ref HEAD; } 2>&1 | tee -a "$OUT" || true
  echo "--- DEBUG: .gitignore presence ---" | tee -a "$OUT"
  { [ -f ".gitignore" ] && { echo ".gitignore exists"; wc -l .gitignore; } || echo "no .gitignore"; } | tee -a "$OUT"
  echo "--- DEBUG: deny rules referencing evidence ---" | tee -a "$OUT"
  { [ -f ".gitignore" ] && grep -nE '(^|/)(evidence)(/|$)|\*evidence\*' .gitignore || true; } | tee -a "$OUT"
  echo "--- DEBUG: allow rules referencing evidence ---" | tee -a "$OUT"
  { [ -f ".gitignore" ] && grep -nE '^!evidence(/|$)|^!evidence/v6\.0(/|$)|^!evidence/v6\.0/distance(/|$)|^!evidence/v6\.0/\*\*' .gitignore || true; } | tee -a "$OUT"
  echo "--- DEBUG: ignored paths under $TARGET_DIR ---" | tee -a "$OUT"
  printf "%s\n" "${IGNORED_LIST:-<none>}" | tee -a "$OUT" >/dev/null
  if [ -n "${IGNORED_LIST:-}" ]; then
    echo "--- DEBUG: check-ignore -v for ignored paths ---" | tee -a "$OUT"
    while IFS= read -r p; do
      [ -z "$p" ] && continue
      printf "%s\n" "$p" | git check-ignore -v --stdin 2>&1 | tee -a "$OUT" || true
    done <<< "$IGNORED_LIST"
  fi
  echo "--- DEBUG: tree of $TARGET_DIR ---" | tee -a "$OUT"
  { [ -d "$TARGET_DIR" ] && ls -la "$TARGET_DIR" || echo "<missing dir>"; } | tr -d $'\r' | tee -a "$OUT"
  exit 1
}

git rev-parse --is-inside-work-tree >/dev/null 2>&1 || { echo "DI4 RESULT=FAIL (not inside a git repo)" | tee "$OUT"; exit 2; }

TO_CHECK="$(printf "%s\n" "$TARGET_DIR"; [ -d "$TARGET_DIR" ] && find "$TARGET_DIR" -maxdepth 1 -type f | sort || true)"
IGNORED_LIST="$(printf "%s\n" $TO_CHECK | git check-ignore --stdin 2>/dev/null || true)"

if [ -n "${IGNORED_LIST// /}" ]; then
  fail "git is ignoring some evidence paths"
fi

{
  echo "git_root=$(git rev-parse --show-toplevel 2>/dev/null)"
  echo "target_dir=$TARGET_DIR"
  echo "--- check-ignore (should be empty) ---"
  printf "%s\n" "$IGNORED_LIST"
  echo "--- directory listing ---"
  { [ -d "$TARGET_DIR" ] && ls -la "$TARGET_DIR" || echo "<missing dir>"; } | tr -d $'\r'
} | tee "$OUT" >/dev/null

echo "DI4 RESULT=PASS" | tee -a "$OUT"
exit 0
