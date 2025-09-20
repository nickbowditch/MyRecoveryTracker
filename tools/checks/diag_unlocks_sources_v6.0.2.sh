#!/bin/bash
OUT="evidence/v6.0/_repo/unlocks_sources.2.txt"
set -euo pipefail
bad=0

echo "=== HARD_CODED_HEADERS(date,unlocks) ===" > "$OUT"
grep -RIn --exclude-dir=build -- 'date,unlocks' app/src/main/java || true | tee -a "$OUT"
if grep -RIn --exclude-dir=build --quiet 'date,unlocks' app/src/main/java; then bad=1; fi

echo >> "$OUT"
echo "=== CALL_SITES(runDailyUnlocks|runAll) ===" | tee -a "$OUT"
grep -RIn --exclude-dir=build -- 'runDailyUnlocks\|runAll\s*(' app/src/main/java || true | tee -a "$OUT"
if grep -RIn --exclude-dir=build --quiet 'runDailyUnlocks\|runAll\s*(' app/src/main/java; then bad=1; fi

echo >> "$OUT"
echo "=== WRITES_TO(daily_unlocks.csv) ===" | tee -a "$OUT"
grep -RIn --exclude-dir=build -- 'daily_unlocks\.csv' app/src/main/java || true | tee -a "$OUT"

if [ $bad -eq 0 ]; then
  echo "DIAG-SOURCES RESULT=PASS" | tee -a "$OUT"
  exit 0
else
  echo "DIAG-SOURCES RESULT=FAIL" | tee -a "$OUT"
  exit 1
fi
