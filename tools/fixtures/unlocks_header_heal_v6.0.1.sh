#!/bin/bash
PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/_repo/unlocks_heal.1.txt"
set -euo pipefail
adb get-state >/dev/null 2>&1 || { echo "UNLOCKS-HEAL RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "UNLOCKS-HEAL RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

LOCK="$(tr -d $'\r' < app/locks/daily_metrics.header 2>/dev/null || true)"
[ -n "$LOCK" ] || { echo "UNLOCKS-HEAL RESULT=FAIL (missing lock header)" | tee "$OUT"; exit 4; }

HEAD="$(adb exec-out run-as "$PKG" sh -c 'head -n 1 files/daily_unlocks.csv 2>/dev/null' | tr -d $'\r' || true)"
if [ -z "$HEAD" ]; then
  echo "UNLOCKS-HEAL RESULT=FAIL (missing daily_unlocks.csv)" | tee "$OUT"; exit 5
fi

if [ "$HEAD" = "$LOCK" ]; then
  echo "UNLOCKS-HEAL RESULT=PASS (already aligned)" | tee "$OUT"; exit 0
fi

if [ "$HEAD" != "date,unlocks" ]; then
  echo "UNLOCKS-HEAL RESULT=FAIL (unexpected current header: $HEAD)" | tee "$OUT"; exit 6
fi

adb exec-out run-as "$PKG" sh -s <<'SH' | tee -a "$OUT"
set -e
IN="files/daily_unlocks.csv"
TMP="files/daily_unlocks.csv.tmp"
OUTF="files/daily_unlocks.csv"
SCHEMA="v6.0"

head -n 1 "\$IN" | tr -d '\r' | grep -qx 'date,unlocks'
tail -n +2 "\$IN" | awk -F, -v OFS=',' -v SCHEMA="\$SCHEMA" '
BEGIN{ print "date,feature_schema_version,daily_unlocks" }
NF>=2{
  d=$1; gsub(/\r/,"",d);
  c=$2; gsub(/\r/,"",c);
  print d, SCHEMA, c
}' > "\$TMP"

rm -f "\$OUTF"
mv "\$TMP" "\$OUTF"
ls -l --time-style=full-iso "\$OUTF" || true
SH

NEWHEAD="$(adb exec-out run-as "$PKG" sh -c 'head -n 1 files/daily_unlocks.csv' | tr -d $'\r')"
echo "NEWHEAD:$NEWHEAD" | tee -a "$OUT"

if [ "$NEWHEAD" = "$LOCK" ]; then
  echo "UNLOCKS-HEAL RESULT=PASS" | tee -a "$OUT"; exit 0
else
  echo "UNLOCKS-HEAL RESULT=FAIL (post-migration header mismatch)" | tee -a "$OUT"; exit 1
fi
