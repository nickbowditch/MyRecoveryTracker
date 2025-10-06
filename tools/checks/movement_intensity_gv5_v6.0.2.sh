#!/bin/bash
set -euo pipefail
OUT="evidence/v6.0/movement_intensity/gv5.txt"
EVDIR="evidence/v6.0/movement_intensity"
mkdir -p "$(dirname "$OUT")" "$EVDIR"
exec > >(tee "$OUT") 2>&1
fail(){ echo "GV5 RESULT=FAIL ($1)"; exit 1; }

echo "started_at=$(date -u +'%Y-%m-%dT%H:%M:%SZ')"

git rev-parse --is-inside-work-tree >/dev/null 2>&1 || fail "not inside git repo"

SENT="$EVDIR/.gv5_sentinel"
echo "gv5-sentinel $(date -u +'%Y-%m-%dT%H:%M:%SZ')" >"$SENT"

echo "--- DEBUG: git check-ignore (verbose) ---"
git check-ignore -v "$EVDIR" "$SENT" 2>/dev/null || true

if git check-ignore -q "$EVDIR" || git check-ignore -q "$SENT"; then
  rm -f "$SENT"
  fail "evidence path ignored by git"
fi

echo "--- DEBUG: git add -n sentinel ---"
GA_OUT="$(git add -n "$SENT" 2>&1 || true)"
printf '%s\n' "$GA_OUT"

case "$GA_OUT" in
  *"$SENT"*) rm -f "$SENT"; echo "GV5 RESULT=PASS"; exit 0 ;;
  *) rm -f "$SENT"; fail "git add dry-run failed (likely ignored)";;
esac
