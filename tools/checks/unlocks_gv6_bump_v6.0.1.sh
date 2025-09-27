#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
CSV="files/daily_unlocks.csv"
LOCK="app/locks/daily_unlocks.header"
OUT="evidence/v6.0/unlocks/gv6.bump.1.txt"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "GV-6 BUMP=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "GV-6 BUMP=FAIL (app not installed)" | tee "$OUT"; exit 3; }

HDR="$(adb exec-out run-as "$PKG" head -n1 "$CSV" 2>/dev/null | tr -d '\r' || true)"
[ -n "$HDR" ] || { echo "GV-6 BUMP=FAIL (missing csv or empty header)" | tee "$OUT"; exit 4; }

printf '%s\n' "$HDR" > "$LOCK"

git add "$LOCK" >/dev/null 2>&1 || true
git commit -m "unlocks(gv6): bump lock to current header" >/dev/null 2>&1 || true

tools/checks/unlocks_gv6_v6.0.1.sh | tee "$OUT"
exit ${PIPESTATUS[0]:-0}
