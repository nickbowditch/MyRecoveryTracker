#!/bin/bash
set -euo pipefail
PKG="com.nick.myrecoverytracker"
CSV="files/daily_movement_intensity.csv"
LOCK="app/locks/daily_movement_intensity.header"
OUT="evidence/v6.0/movement_intensity/gv6.txt"

mkdir -p "$(dirname "$OUT")" "app/locks"
exec > >(tee "$OUT") 2>&1
fail(){ echo "GV6 RESULT=FAIL ($1)"; exit 1; }

echo "started_at=$(date -u +'%Y-%m-%dT%H:%M:%SZ')"

adb get-state >/dev/null 2>&1 || fail "no device"
adb shell pm path "$PKG" >/dev/null 2>&1 || fail "app not installed"

HDR="$(adb exec-out run-as "$PKG" sed -n '1p' "$CSV" 2>/dev/null | tr -d '\r' || true)"
[ -n "$HDR" ] || fail "daily header missing"
echo "daily_header=$HDR"

PRE="<none>"
[ -f "$LOCK" ] && PRE="$(sed -n '1p' "$LOCK" | tr -d '\r' || true)"
echo "prev_lock_header=$PRE"

printf '%s\n' "$HDR" > "$LOCK"

POST="$(sed -n '1p' "$LOCK" | tr -d '\r' || true)"
echo "new_lock_header=$POST"

if [ "$POST" != "$HDR" ]; then
  fail "lock file not updated to match daily header"
fi

echo "--- DEBUG: CSV head ---"
adb exec-out run-as "$PKG" head -n 5 "$CSV" 2>/dev/null | tr -d '\r' || true
echo "--- DEBUG: LOCK file ---"
sed -n '1,3p' "$LOCK" | tr -d '\r' || true

echo "GV6 RESULT=PASS"
exit 0
