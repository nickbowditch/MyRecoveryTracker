#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/_repo/csv_tail.1.txt"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "CSV-TAIL RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "CSV-TAIL RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

FILES="$(adb exec-out run-as "$PKG" sh -c 'ls -1 files/*.csv 2>/dev/null' | tr -d '\r' || true)"

if [ -z "$FILES" ]; then
  echo "CSV-TAIL RESULT=FAIL (no CSVs found)" | tee "$OUT"
  exit 1
fi

{
  echo "CSV-TAIL RESULT=PASS"
  echo "--- LAST 3 ROWS PER CSV ---"
  for f in $FILES; do
    echo
    echo "### $f ###"
    adb exec-out run-as "$PKG" tail -n 3 "$f" 2>/dev/null | tr -d '\r' || echo "(unreadable or empty)"
  done
} | tee "$OUT"

exit 0
