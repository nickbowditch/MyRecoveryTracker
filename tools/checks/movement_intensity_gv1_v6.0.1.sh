#!/bin/bash
set -euo pipefail
PKG="com.nick.myrecoverytracker"
DIR="tools/checks"
EVDIR="evidence/v6.0/movement_intensity"
OUT="$EVDIR/gv1.txt"

mkdir -p "$(dirname "$OUT")" "$EVDIR"
exec > >(tee "$OUT") 2>&1
fail(){ echo "GV1 RESULT=FAIL ($1)"; exit 1; }

adb get-state >/dev/null 2>&1 || { echo "GV1 RESULT=FAIL (no device)"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "GV1 RESULT=FAIL (app not installed)"; exit 3; }

SCRIPTS="$(ls "$DIR"/movement_intensity_*.sh 2>/dev/null || true)"
[ -n "$SCRIPTS" ] || fail "no movement_intensity scripts found in $DIR"

echo "--- DEBUG: Scripts found ---"
printf '%s\n' "$SCRIPTS"

MISSING=0
for f in $SCRIPTS; do
  [ -f "$f" ] || { echo "missing $f"; MISSING=1; continue; }
  [ -x "$f" ] || { echo "not executable $f"; MISSING=1; continue; }
done

if [ "$MISSING" -ne 0 ]; then
  fail "some movement_intensity checks missing or not executable"
fi

echo "GV1 RESULT=PASS"
exit 0
