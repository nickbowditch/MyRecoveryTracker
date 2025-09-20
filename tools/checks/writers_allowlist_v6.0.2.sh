#!/bin/bash
OUT="evidence/v6.0/_repo/writers_allowlist.2.txt"
fail=0

ALLOW_UNLOCKS='(UnlockWorker\.kt|UnlockRollupWorker\.kt|Csv\.kt|CsvUtils\.kt|UnlockValidationWorker\.kt|RedcapUploadWorker\.kt|UnlockMigrations\.kt|LogRetentionWorker\.kt|HealthSnapshotWorker\.kt|TriggerReceiver\.kt)'
ALLOW_SLEEP='(Sleep.*\.kt|Csv\.kt|CsvUtils\.kt|.*ValidationWorker\.kt|RedcapUploadWorker\.kt|LogRetentionWorker\.kt|HealthSnapshotWorker\.kt|TriggerReceiver\.kt)'

echo "SCAN_DIR: app/src/main/java" | tee "$OUT"

grep -RIn -E --exclude-dir=build -- 'daily_unlocks\.csv' app/src/main/java | tee -a "$OUT" || true
if grep -RIn -E --exclude-dir=build -- 'daily_unlocks\.csv' app/src/main/java | grep -vE "$ALLOW_UNLOCKS" >/dev/null; then
  echo "UNLOCKS_WRITER_DRIFT" | tee -a "$OUT"
  fail=1
fi

grep -RIn -E --exclude-dir=build -- 'daily_sleep\.csv' app/src/main/java | tee -a "$OUT" || true
if grep -RIn -E --exclude-dir=build -- 'daily_sleep\.csv' app/src/main/java | grep -vE "$ALLOW_SLEEP" >/dev/null; then
  echo "SLEEP_WRITER_DRIFT" | tee -a "$OUT"
  fail=1
fi

if [ "$fail" -eq 0 ]; then
  echo "WRITERS-ALLOWLIST RESULT=PASS" | tee -a "$OUT"; exit 0
else
  echo "WRITERS-ALLOWLIST RESULT=FAIL" | tee -a "$OUT"; exit 1
fi
