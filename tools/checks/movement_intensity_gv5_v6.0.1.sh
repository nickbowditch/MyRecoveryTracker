#!/bin/sh
set -eu
OUT="evidence/v6.0/movement_intensity/gv5.txt"
mkdir -p "$(dirname "$OUT")"
exec > >(tee "$OUT") 2>&1
fail(){ echo "GV5 RESULT=FAIL ($1)"; exit 1; }

git rev-parse --is-inside-work-tree >/dev/null 2>&1 || fail "not inside git repo"
EVDIR="evidence/v6.0/movement_intensity"
SENT="$EVDIR/.gv5_sentinel"
echo "gv5-sentinel $(date -u +'%Y-%m-%dT%H:%M:%SZ')" >"$SENT"

echo "--- DEBUG: git check-ignore ---"
git check-ignore -v "$EVDIR" "$SENT" 2>/dev/null || true

if git check-ignore -q "$EVDIR" || git check-ignore -q "$SENT"; then
  rm -f "$SENT"
  fail "evidence path ignored by git"
fi

echo "--- DEBUG: git add dry-run ---"
GA_OUT="$(git add -n "$SENT" 2>&1 || true)"
printf '%s\n' "$GA_OUT"

case "$GA_OUT" in
  *"$SENT"*) echo "GV5 RESULT=PASS"; rm -f "$SENT"; exit 0 ;;
  *) rm -f "$SENT"; fail "git add dry-run failed (ignored path)";;
esac
