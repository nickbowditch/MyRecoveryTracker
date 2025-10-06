#!/bin/bash
set -euo pipefail
PKG="com.nick.myrecoverytracker"
EVDIR="evidence/v6.0/movement_intensity"
OUT="$EVDIR/at3.txt"

mkdir -p "$(dirname "$OUT")" "$EVDIR"
exec > >(tee "$OUT") 2>&1
fail(){ echo "AT3 RESULT=FAIL ($1)"; exit 1; }

echo "started_at=$(date -u +'%Y-%m-%dT%H:%M:%SZ')"

adb get-state >/dev/null 2>&1 || { echo "AT3 RESULT=FAIL (no device)"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "AT3 RESULT=FAIL (app not installed)"; exit 3; }

git rev-parse --is-inside-work-tree >/dev/null 2>&1 || fail "not inside a git repository"
REPO_ROOT="$(git rev-parse --show-toplevel 2>/dev/null || echo "<unknown>")"
echo "repo_root=$REPO_ROOT"

SENT="$EVDIR/.at3_git_add_probe"
echo "at3-sentinel $(date -u +'%Y-%m-%dT%H:%M:%SZ')" > "$SENT"

CI_DIR_VERBOSE="$(git check-ignore -v "$EVDIR" 2>/dev/null || true)"
CI_FILE_VERBOSE="$(git check-ignore -v "$SENT" 2>/dev/null || true)"
git check-ignore -q "$EVDIR"; CI_DIR_STATUS=$?
git check-ignore -q "$SENT";  CI_FILE_STATUS=$?

GA_DRYRUN="$(git add -n "$SENT" 2>&1 || true)"
STATUS_PORC="$(git status --porcelain --ignored "$SENT" 2>/dev/null || true)"

echo "--- DEBUG: git check-ignore verbose (dir) ---"
[ -n "$CI_DIR_VERBOSE" ] && echo "$CI_DIR_VERBOSE" || echo "<none>"
echo "--- DEBUG: git check-ignore verbose (sentinel) ---"
[ -n "$CI_FILE_VERBOSE" ] && echo "$CI_FILE_VERBOSE" || echo "<none>"
echo "--- DEBUG: git check-ignore status codes ---"
echo "dir_status=$CI_DIR_STATUS (0=ignored,1=not ignored,128=error)"
echo "file_status=$CI_FILE_STATUS (0=ignored,1=not ignored,128=error)"
echo "--- DEBUG: git add -n sentinel ---"
[ -n "$GA_DRYRUN" ] && echo "$GA_DRYRUN" || echo "<none>"
echo "--- DEBUG: git status --porcelain --ignored (sentinel) ---"
[ -n "$STATUS_PORC" ] && echo "$STATUS_PORC" || echo "<none>"

if [ "$CI_DIR_STATUS" -eq 0 ] || [ "$CI_FILE_STATUS" -eq 0 ]; then
  rm -f "$SENT"
  fail "evidence path is ignored by git"
fi

case "$GA_DRYRUN" in
  *"$SENT"*) : ;;
  *) rm -f "$SENT"; fail "git would not stage sentinel (likely ignored)";;
esac

rm -f "$SENT"
echo "AT3 RESULT=PASS"
exit 0
