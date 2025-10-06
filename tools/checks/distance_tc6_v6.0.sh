#!/bin/bash
set -euo pipefail
PKG="com.nick.myrecoverytracker"
TARGET_DIR="evidence/v6.0/distance"
OUT="evidence/v6.0/distance/tc6.txt"
mkdir -p "$(dirname "$OUT")"
fail() {
  echo "TC6 RESULT=FAIL ($1)" | tee "$OUT"
  echo "--- DEBUG: git root / branch ---" | tee -a "$OUT"
  { git rev-parse --show-toplevel && git rev-parse --abbrev-ref HEAD; } 2>&1 | tee -a "$OUT" || true
  echo "--- DEBUG: .gitignore presence ---" | tee -a "$OUT"
  { [ -f ".gitignore" ] && { echo ".gitignore exists"; wc -l .gitignore; } || echo "no .gitignore"; } | tee -a "$OUT"
  echo "--- DEBUG: .gitignore allow rules for evidence ---" | tee -a "$OUT"
  { [ -f ".gitignore" ] && grep -nE '^!evidence(/|$)|^!evidence/v6\.0(/|$)|^!evidence/v6\.0/distance(/|$)|^!evidence/v6\.0/\*\*' .gitignore || true; } | tee -a "$OUT"
  echo "--- DEBUG: git check-ignore (verbose) for TARGET_DIR and its files ---" | tee -a "$OUT"
  { printf "%s\n" "$TARGET_DIR"; [ -d "$TARGET_DIR" ] && find "$TARGET_DIR" -maxdepth 1 -type f | sort || true; } | git check-ignore -v --stdin 2>&1 | tee -a "$OUT" || true
  echo "--- DEBUG: tree of $TARGET_DIR ---" | tee -a "$OUT"
  { [ -d "$TARGET_DIR" ] && ls -la "$TARGET_DIR" || echo "<missing dir>"; } | tr -d $'\r' | tee -a "$OUT"
  exit 1
}
echo "[INFO] TC6 — Distance Evidence tracked (not ignored by git)"
git rev-parse --is-inside-work-tree >/dev/null 2>&1 || { echo "TC6 RESULT=FAIL (not inside a git repo)" | tee "$OUT"; exit 2; }
TO_CHECK="$(printf "%s\n" "$TARGET_DIR"; [ -d "$TARGET_DIR" ] && find "$TARGET_DIR" -maxdepth 1 -type f | sort || true)"
CHK_VERBOSE="$(printf "%s\n" "$TO_CHECK" | git check-ignore -v --stdin 2>/dev/null || true)"
BAD_LINES=""
ALLOW_ONLY=1
if [ -n "${CHK_VERBOSE// /}" ]; then
  while IFS= read -r line; do
    pat="$(printf "%s" "$line" | sed -E 's/^[^:]+:[^:]+:(.*)\t.*$/\1/')"
    if [ -z "$pat" ] || [ "${pat#*!}" = "$pat" ]; then
      ALLOW_ONLY=0
      BAD_LINES+="$line"$'\n'
    fi
  done <<< "$CHK_VERBOSE"
fi
if [ -n "${BAD_LINES// /}" ]; then
  fail "git check-ignore reports actual ignore rules affecting $TARGET_DIR"
fi
HAS_DENY="$( [ -f ".gitignore" ] && grep -Eq '(^|/)(evidence)(/|$)|\*evidence\*' .gitignore && echo 1 || echo 0 )"
HAS_ALLOW="$( [ -f ".gitignore" ] && grep -Eq '^!evidence(/|$)|^!evidence/v6\.0(/|$)|^!evidence/v6\.0/distance(/|$)|^!evidence/v6\.0/\*\*' .gitignore && echo 1 || echo 0 )"
if [ "$HAS_DENY" -eq 1 ] && [ "$HAS_ALLOW" -eq 0 ]; then
  fail "found .gitignore deny for evidence but no whitelist for $TARGET_DIR"
fi
{
  echo "git_root=$(git rev-parse --show-toplevel 2>/dev/null)"
  echo "target_dir=$TARGET_DIR"
  echo "deny_rule_present=$HAS_DENY"
  echo "allow_rule_present=$HAS_ALLOW"
  echo "--- check-ignore summary ---"
  if [ -z "${CHK_VERBOSE// /}" ]; then
    echo "(no paths under $TARGET_DIR are ignored)"
  else
    echo "(only negation rules matched; not ignored)"
    printf "%s\n" "$CHK_VERBOSE"
  fi
  echo "--- directory listing ---"
  { [ -d "$TARGET_DIR" ] && ls -la "$TARGET_DIR" || echo "<missing dir>"; } | tr -d $'\r'
} | tee "$OUT" >/dev/null
echo "TC6 RESULT=PASS" | tee -a "$OUT"
exit 0
