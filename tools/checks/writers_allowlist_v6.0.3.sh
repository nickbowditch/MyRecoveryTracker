#!/bin/bash
OUT="evidence/v6.0/_repo/writers_allowlist.3.txt"
fail=0

ALLOW_UNLOCKS='(UnlockWorker\.kt|UnlockRollupWorker\.kt|UnlockValidationWorker\.kt|RedcapUploadWorker\.kt|UnlockMigrations\.kt|LogRetentionWorker\.kt|HealthSnapshotWorker\.kt|TriggerReceiver\.kt|Csv\.kt|CsvUtils\.kt)'
ALLOW_SLEEP='(Sleep.*\.kt|.*ValidationWorker\.kt|RedcapUploadWorker\.kt|LogRetentionWorker\.kt|HealthSnapshotWorker\.kt|TriggerReceiver\.kt|Csv\.kt|CsvUtils\.kt)'

WRITE_SIG='(writeText|appendText|bufferedWriter|outputStream|FileOutputStream|renameTo)'

echo "SCAN_DIR: app/src/main/java" | tee "$OUT"

unlock_files=$(grep -RIl -E --exclude-dir=build 'daily_unlocks\.csv' app/src/main/java || true)
if [ -n "$unlock_files" ]; then
  for f in $unlock_files; do
    if grep -qE "$WRITE_SIG" "$f"; then
      if ! echo "$f" | grep -qE "$ALLOW_UNLOCKS"; then
        echo "UNLOCKS_WRITER_DRIFT:$f" | tee -a "$OUT"
        fail=1
      fi
    fi
  done
fi

sleep_files=$(grep -RIl -E --exclude-dir=build 'daily_sleep(\.csv|_duration\.csv)' app/src/main/java || true)
if [ -n "$sleep_files" ]; then
  for f in $sleep_files; do
    if grep -qE "$WRITE_SIG" "$f"; then
      if ! echo "$f" | grep -qE "$ALLOW_SLEEP"; then
        echo "SLEEP_WRITER_DRIFT:$f" | tee -a "$OUT"
        fail=1
      fi
    fi
  done
fi

if [ "$fail" -eq 0 ]; then
  echo "WRITERS-ALLOWLIST RESULT=PASS" | tee -a "$OUT"; exit 0
else
  echo "WRITERS-ALLOWLIST RESULT=FAIL" | tee -a "$OUT"; exit 1
fi
