#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/_repo/csv_audit.1.txt"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "CSV-AUDIT RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "CSV-AUDIT RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

# List all *.csv under app files/
FILES="$(adb exec-out run-as "$PKG" sh -c 'ls -1 files/*.csv 2>/dev/null' | tr -d '\r' || true)"

if [ -z "$FILES" ]; then
  echo "CSV-AUDIT RESULT=FAIL (no CSVs found)" | tee "$OUT"
  exit 1
fi

{
  echo "CSV-AUDIT RESULT=PASS"
  echo "--- CSV FILES ---"
  echo "$FILES"
} | tee "$OUT"

exit 0
