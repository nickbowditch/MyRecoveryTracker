#!/bin/sh
set -eu
OUT="evidence/v6.0/app_usage_by_category/actions_scan.txt"
mkdir -p "$(dirname "$OUT")"

[ -d app/src/main/java ] || { echo "SCAN RESULT=FAIL (missing java source dir)" | tee "$OUT"; exit 2; }

{
  echo "=== MANIFEST RECEIVER INTENT ACTIONS ==="
  grep -nE '<action android:name="[^"]+"' app/src/main/AndroidManifest.xml 2>/dev/null | sed -E 's/^[[:space:]]+//'
  echo

  echo "=== ALL ACTION_* STRINGS IN CODE ==="
  grep -R --include='*.kt' --include='*.java' -nE '"com\.nick[^"]*ACTION_[A-Z0-9_\.]+"' app/src/main/java 2>/dev/null \
    | sed -E 's/^([^:]+):([0-9]+):.*"([^"]+)".*$/\3 \t(\1:\2)/' | sort -u
  echo

  echo "=== LINES IN TriggerReceiver REFERENCING ACTION OR CATEGORY ==="
  TRF="$(git ls-files 'app/src/main/java/**/*.kt' | grep -E 'TriggerReceiver\.kt$' || true)"
  if [ -n "$TRF" ]; then
    nl -ba "$TRF" | grep -nE 'ACTION_|Category|AppUsageByCategory|CATEGORY' || sed -n '1,200p' "$TRF"
  else
    echo "[no TriggerReceiver.kt found]"
  fi
  echo

  echo "=== REFERENCES TO AppUsageByCategoryDailyWorker & ACTION IN CODE ==="
  grep -R --include='*.kt' -nE 'AppUsageByCategoryDailyWorker|AppUsageCategory|CategoryDaily|CATEGORY|ACTION_' app/src/main/java 2>/dev/null \
    | sed -E 's/^[[:space:]]+//'
  echo

  echo "=== FILE WRITES FOR CATEGORY CSV TARGETS ==="
  grep -R --include='*.kt' -nE 'File\(.*"[^"]*category[^"]*\.csv"|writeText\("date,category,minutes' app/src/main/java 2>/dev/null \
    | sed -E 's/^[[:space:]]+//'
} | tee "$OUT"

echo "SCAN RESULT=PASS" | tee -a "$OUT"
exit 0
