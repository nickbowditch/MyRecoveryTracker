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

ADD_OUT="$(git add -n "$SENT" 2>&1 || true)"
CHK_DIR="$(git check-ignore -v "$EVDIR" 2>/dev/null || true)"
CHK_FILE="$(git check-ignore -v "$SENT" 2>/dev/null || true)"
STATUS_PORC="$(git status --porcelain --ignored "$SENT" 2>/dev/null || true)"

echo "--- DEBUG: git add -n output ---"
[ -n "$ADD_OUT" ] && echo "$ADD_OUT" || echo "<none>"
echo "--- DEBUG: git check-ignore (dir) ---"
[ -n "$CHK_DIR" ] && echo "$CHK_DIR" || echo "<none>"
echo "--- DEBUG: git check-ignore (file) ---"
[ -n "$CHK_FILE" ] && echo "$CHK_FILE" || echo "<none>"
echo "--- DEBUG: git status --porcelain --ignored ---"
[ -n "$STATUS_PORC" ] && echo "$STATUS_PORC" || echo "<none>"

case "$ADD_OUT" in
  *"$SENT"*)
    rm -f "$SENT"
    echo "AT3 RESULT=PASS"
    exit 0
    ;;
  *)
    rm -f "$SENT"
    fail "git would not stage sentinel (likely ignored)"
    ;;
esac
