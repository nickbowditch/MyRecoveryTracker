#!/bin/bash
PKG="com.nick.myrecoverytracker"
adb get-state >/dev/null 2>&1 || { echo "DI-1-FIX RESULT=FAIL (no device)"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "DI-1-FIX RESULT=FAIL (app not installed)"; exit 3; }
LOCK="$(tr -d '\r' < app/locks/daily_metrics.header 2>/dev/null)"
[ -n "$LOCK" ] || { echo "DI-1-FIX RESULT=FAIL (missing lock header)"; exit 4; }
SCHEMA_VER="v6.0"
adb exec-out run-as "$PKG" sh -s "$LOCK" "$SCHEMA_VER" <<'SH'
set -e
LOCK="$1"
VER="$2"
F=files/daily_unlocks.csv
[ -f "$F" ] || { echo "DI-1-FIX RESULT=FAIL (missing csv)"; exit 5; }
HEAD="$(head -n 1 "$F")"
if [ "$HEAD" = "$LOCK" ]; then
  echo "DI-1-FIX RESULT=PASS (already locked)"
  exit 0
fi
if [ "$HEAD" = "date,unlocks" ]; then
  TMP="$F.tmp.$$"
  printf "%s\n" "$LOCK" > "$TMP"
  tail -n +2 "$F" | awk -F, -v v="$VER" 'NF>=2{printf "%s,%s,%s\n",$1,v,$2}' >> "$TMP"
  mv -f "$TMP" "$F"
  echo "DI-1-FIX RESULT=PASS (migrated)"
  exit 0
fi
echo "DI-1-FIX RESULT=FAIL (unexpected header: $HEAD)"
exit 1
SH
