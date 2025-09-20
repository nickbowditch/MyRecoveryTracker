#!/bin/bash
OUT="evidence/v6.0/_repo/unlocks_writers.1.txt"
set -euo pipefail
{
  echo "=== HARD-CODED HEADERS ==="
  grep -RIn --exclude-dir=build -- 'date,unlocks' app/src/main/java || true
  echo
  echo "=== WRITES TO daily_unlocks.csv ==="
  grep -RIn --exclude-dir=build -- 'daily_unlocks\.csv' app/src/main/java || true
} | tee "$OUT"
