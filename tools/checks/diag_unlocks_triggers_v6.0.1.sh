#!/bin/bash
OUT="evidence/v6.0/_repo/unlocks_triggers.1.txt"
set -euo pipefail
{
  echo "=== ENQUEUE CALLS (WorkManager/Unlock*) ==="
  grep -RIn --exclude-dir=build -- 'WorkManager|enqueue|UniqueWork|UnlockWorker|UnlockRollupWorker' app/src/main/java || true
  echo
  echo "=== RECEIVERS & TRIGGERS (TriggerReceiver.*, ACTION_*) ==="
  grep -RIn --exclude-dir=build -- 'TriggerReceiver|ACTION_RUN_UNLOCK' app/src/main/java || true
  echo
  echo "=== ANY rollup helper calls ==="
  grep -RIn --exclude-dir=build -- 'rollup|Rollup' app/src/main/java || true
} | tee "$OUT"

echo "DIAG-TRIGGERS RESULT=PASS" | tee -a "$OUT"
