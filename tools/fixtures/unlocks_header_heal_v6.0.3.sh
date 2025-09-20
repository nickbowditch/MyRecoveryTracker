#!/bin/bash
PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/_repo/unlocks_heal.3.txt"
set -euo pipefail

adb get-state >/dev/null 2>&1 || { echo "HEAL RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "HEAL RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

LOCK="$(tr -d $'\r' < app/locks/daily_metrics.header 2>/dev/null || true)"
HEAD="$(adb exec-out run-as "$PKG" sh -c 'head -n 1 files/daily_unlocks.csv 2>/dev/null' | tr -d $'\r' || true)"

{
  echo "LOCK:$LOCK"
  echo "HEAD:$HEAD"
} | tee "$OUT"

if [ -z "$HEAD" ]; then echo "HEAL RESULT=FAIL (missing csv)" | tee -a "$OUT"; exit 4; fi
if [ "$HEAD" = "$LOCK" ]; then echo "HEAL RESULT=PASS (already aligned)" | tee -a "$OUT"; exit 0; fi
if [ "$HEAD" != "date,unlocks" ]; then echo "HEAL RESULT=FAIL (unexpected header: $HEAD)" | tee -a "$OUT"; exit 5; fi

CSV="$(adb exec-out run-as "$PKG" cat files/daily_unlocks.csv | tr -d $'\r')"
NEW="$(printf '%s\n' "$CSV" | awk -F, -v OFS=',' -v S="v6.0" '
NR==1 { print "date,feature_schema_version,daily_unlocks"; next }
NF>=2 { print $1, S, $2 }
')"

printf "%s\n" "$NEW" | adb shell run-as "$PKG" 'cat > files/daily_unlocks.csv.tmp'
adb shell run-as "$PKG" 'rm -f files/daily_unlocks.csv && mv files/daily_unlocks.csv.tmp files/daily_unlocks.csv'

NEWHEAD="$(adb exec-out run-as "$PKG" head -n 1 files/daily_unlocks.csv | tr -d $'\r')"
echo "NEWHEAD:$NEWHEAD" | tee -a "$OUT"

if [ "$NEWHEAD" = "$LOCK" ]; then
  echo "HEAL RESULT=PASS" | tee -a "$OUT"; exit 0
else
  echo "HEAL RESULT=FAIL (post-migration mismatch)" | tee -a "$OUT"; exit 1
fi
