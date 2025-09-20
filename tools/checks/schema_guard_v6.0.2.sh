#!/bin/bash
PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/_repo/schema_guard.2.txt"

adb get-state >/dev/null 2>&1 || { echo "SCHEMA-GUARD RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "SCHEMA-GUARD RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

fail=0
echo "LOCKS_DIR: app/locks" | tee "$OUT"

for LOCKF in app/locks/*.header; do
  [ -f "$LOCKF" ] || continue
  BASE="$(basename "$LOCKF" .header)"
  CSVF="files/${BASE}.csv"
  LOCK_HDR="$(tr -d $'\r' < "$LOCKF" 2>/dev/null)"
  DEV_HDR="$(adb exec-out run-as "$PKG" sh -c 'head -n 1 "'"$CSVF"'" 2>/dev/null' | tr -d $'\r')"

  echo "FEATURE=$BASE" | tee -a "$OUT"
  echo "  CSV_FILE=$CSVF" | tee -a "$OUT"
  echo "  LOCK_HEADER=$LOCK_HDR" | tee -a "$OUT"
  echo "  CSV_HEADER=$DEV_HDR" | tee -a "$OUT"

  if [ -z "$DEV_HDR" ]; then
    echo "  RESULT=FAIL (missing csv)" | tee -a "$OUT"; fail=1; continue
  fi
  if [ "$DEV_HDR" != "$LOCK_HDR" ]; then
    echo "  RESULT=FAIL (header drift)" | tee -a "$OUT"; fail=1; continue
  fi
  echo "  RESULT=PASS" | tee -a "$OUT"
done

if [ "$fail" -eq 0 ]; then
  echo "SCHEMA-GUARD RESULT=PASS" | tee -a "$OUT"; exit 0
else
  echo "SCHEMA-GUARD RESULT=FAIL" | tee -a "$OUT"; exit 1
fi
