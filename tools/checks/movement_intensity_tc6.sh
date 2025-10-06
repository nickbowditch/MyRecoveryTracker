#!/bin/bash
set -euo pipefail
PKG="com.nick.myrecoverytracker"
EVDIR="evidence/v6.0/movement_intensity"
OUT="$EVDIR/tc6.txt"

mkdir -p "$(dirname "$OUT")" "$EVDIR"
exec > >(tee "$OUT") 2>&1
fail(){ echo "TC6 RESULT=FAIL ($1)"; exit 1; }

adb get-state >/dev/null 2>&1 || { echo "TC6 RESULT=FAIL (no device)"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "TC6 RESULT=FAIL (app not installed)"; exit 3; }

git rev-parse --is-inside-work-tree >/dev/null 2>&1 || fail "not inside a git repository"

SENT="$EVDIR/.tc6_track_sentinel"
echo "tc6-sentinel" > "$SENT"

if git check-ignore "$SENT" >/dev/null 2>&1; then
  rm -f "$SENT"
  fail "sentinel ignored by git"
fi

if ! git add -n "$SENT" >/dev/null 2>&1; then
  rm -f "$SENT"
  fail "sentinel not addable"
fi

rm -f "$SENT"
echo "TC6 RESULT=PASS"
exit 0
