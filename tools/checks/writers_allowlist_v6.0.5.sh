#!/bin/bash
OUT="evidence/v6.0/_repo/writers_allowlist.5.txt"
fail=0

ALLOW_UNLOCKS='(UnlockWorker\.kt|UnlockRollupWorker\.kt|UnlockValidationWorker\.kt|RedcapUploadWorker\.kt|UnlockMigrations\.kt|LogRetentionWorker\.kt|HealthSnapshotWorker\.kt|TriggerReceiver\.kt|Csv\.kt|CsvUtils\.kt|RollupValidator\.kt)'
ALLOW_SLEEP='(Sleep.*\.kt|.*ValidationWorker\.kt|RedcapUploadWorker\.kt|LogRetentionWorker\.kt|HealthSnapshotWorker\.kt|TriggerReceiver\.kt|Csv\.kt|CsvUtils\.kt)'
WRITE_SIG='(writeText|appendText|bufferedWriter|outputStream|FileOutputStream|renameTo)'

echo "SCAN_DIR: app/src/main/java" | tee "$OUT"

unlock_write_lines=$(grep -RIn -E --exclude-dir=build "daily_unlocks\.csv.*$WRITE_SIG|$WRITE_SIG.*daily_unlocks\.csv" app/src/main/java 2>/dev/null || true)
sleep_write_lines=$(grep -RIn -E --exclude-dir=build "daily_sleep(_duration)?\.csv.*$WRITE_SIG|$WRITE_SIG.*daily_sleep(_duration)?\.csv" app/src/main/java 2>/dev/null || true)

if [ -n "$unlock_write_lines" ]; then
  printf "%s\n" "$unlock_write_lines" | tee -a "$OUT"
  unlock_files=$(printf "%s\n" "$unlock_write_lines" | cut -d: -f1 | sort -u)
  for f in $unlock_files; do
    if ! echo "$f" | grep -qE "$ALLOW_UNLOCKS"; then
      echo "UNLOCKS_WRITER_DRIFT:$f" | tee -a "$OUT"
      fail=1
    fi
  done
fi

if [ -n "$sleep_write_lines" ]; then
  printf "%s\n" "$sleep_write_lines" | tee -a "$OUT"
  sleep_files=$(printf "%s\n" "$sleep_write_lines" | cut -d: -f1 | sort -u)
  for f in $sleep_files; do
    if ! echo "$f" | grep -qE "$ALLOW_SLEEP"; then
      echo "SLEEP_WRITER_DRIFT:$f" | tee -a "$OUT"
      fail=1
    fi
  done
fi

if [ "$fail" -eq 0 ]; then
  echo "WRITERS-ALLOWLIST RESULT=PASS" | tee -a "$OUT"; exit 0
else
  echo "WRITERS-ALLOWLIST RESULT=FAIL" | tee -a "$OUT"; exit 1
fi
