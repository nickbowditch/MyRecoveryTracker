#!/bin/bash
set -euo pipefail

TARGET_DIR="evidence/v6.0/distance"
OUT="$TARGET_DIR/gv4.txt"
mkdir -p "$(dirname "$OUT")" "$TARGET_DIR"

fail() {
  echo "GV4 RESULT=FAIL ($1)" | tee "$OUT"
  echo "--- DEBUG: directory listing ---" | tee -a "$OUT"
  { [ -d "$TARGET_DIR" ] && ls -la "$TARGET_DIR" || echo "<missing dir>"; } | tee -a "$OUT"
  echo "--- DEBUG: RESULT lines (last 100) ---" | tee -a "$OUT"
  { for f in $(find "$TARGET_DIR" -maxdepth 1 -type f -name '*.txt' 2>/dev/null | sort); do
      grep -Hn 'RESULT=' "$f" 2>/dev/null || true
    done | tail -n 100; } | tee -a "$OUT"
  exit 1
}

TXT_LIST="$(find "$TARGET_DIR" -maxdepth 1 -type f -name '*.txt' 2>/dev/null | sort || true)"
TOTAL_TXT="$(printf "%s\n" "$TXT_LIST" | sed '/^$/d' | wc -l | tr -d '[:space:]')"
[ "${TOTAL_TXT:-0}" -gt 0 ] || fail "no evidence .txt files present"

PASS_FILES=0
PASS_LINES=0
for f in $TXT_LIST; do
  if grep -q 'RESULT=PASS' "$f" 2>/dev/null; then
    PASS_FILES=$((PASS_FILES+1))
    PASS_LINES=$((PASS_LINES + $(grep -c 'RESULT=PASS' "$f" 2>/dev/null || echo 0)))
  fi
done

{
  echo "GV4 CHECK: PASS produces evidence"
  echo "dir=$TARGET_DIR"
  echo "files_total=$TOTAL_TXT"
  echo "files_with_PASS=$PASS_FILES"
  echo "PASS_lines_total=$PASS_LINES"
  echo "--- RESULT lines (first 50) ---"
  for f in $TXT_LIST; do
    grep -Hn 'RESULT=' "$f" 2>/dev/null || true
  done | sed -n '1,50p'
} | tee "$OUT" >/dev/null

[ "${PASS_FILES:-0}" -gt 0 ] || fail "no files contain RESULT=PASS"

echo "GV4 RESULT=PASS" | tee -a "$OUT"
exit 0
