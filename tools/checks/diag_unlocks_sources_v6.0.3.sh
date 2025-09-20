#!/bin/bash
OUT="evidence/v6.0/_repo/unlocks_sources.3.txt"
set -euo pipefail
bad=0
{
  echo "=== HARD_CODED_HEADERS(date,unlocks) ==="
  grep -RIn -E --exclude-dir=build -- '(^|[^A-Z_])date,unlocks($|[^A-Z_])' app/src/main/java || true
  echo
  echo "=== WRITES_TO(daily_unlocks.csv) ==="
  grep -RIn -E --exclude-dir=build -- 'daily_unlocks\.csv' app/src/main/java || true
} | tee "$OUT"
if grep -RIn -E --exclude-dir=build --quiet '(^|[^A-Z_])date,unlocks($|[^A-Z_])' app/src/main/java; then bad=1; fi
if [ $bad -eq 0 ]; then echo "DIAG-SOURCES RESULT=PASS" | tee -a "$OUT"; exit 0; else echo "DIAG-SOURCES RESULT=FAIL" | tee -a "$OUT"; exit 1; fi
