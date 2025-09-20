#!/bin/bash
OUT="evidence/v6.0/_repo/unlocks_callers.1.txt"
set -euo pipefail
bad=0
grep -RIn --exclude-dir=build -- 'Rollups\.runAll\s*\(|Rollups\.runDailyUnlocks\s*\(' app/src/main/java | tee "$OUT" || true
if [ -s "$OUT" ]; then bad=1; fi
if [ $bad -eq 0 ]; then
  echo "DIAG-CALLERS RESULT=PASS" | tee -a "$OUT"
  exit 0
else
  echo "DIAG-CALLERS RESULT=FAIL" | tee -a "$OUT"
  exit 1
fi
