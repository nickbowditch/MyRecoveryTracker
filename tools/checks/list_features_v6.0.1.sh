#!/bin/bash
PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/_repo/features.list.1.txt"

adb get-state >/dev/null 2>&1 || { echo "FEATURES RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "FEATURES RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

echo "LOCKS_DIR: app/locks" | tee "$OUT"
for h in app/locks/*.header; do
  [ -f "$h" ] || continue
  b="${h##*/}"; b="${b%.header}"
  csv="files/$b.csv"
  lock="$(tr -d $'\r' < "$h" 2>/dev/null)"
  head="$(adb exec-out run-as "$PKG" sh -c 'head -n 1 '"$csv"' 2>/dev/null' | tr -d $'\r')"
  echo "FEATURE=$b" | tee -a "$OUT"
  echo "  LOCK_FILE=$h" | tee -a "$OUT"
  echo "  CSV_FILE=$csv" | tee -a "$OUT"
  echo "  LOCK_HEADER=$lock" | tee -a "$OUT"
  echo "  CSV_HEADER=$head" | tee -a "$OUT"
done

echo "FEATURES RESULT=PASS" | tee -a "$OUT"
