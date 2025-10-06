#!/bin/sh
set -eu
OUT="evidence/v6.0/app_usage_by_category/greprefs.txt"
mkdir -p "$(dirname "$OUT")"

[ -d app/src/main/java ] || { echo "GREP-REFS RESULT=FAIL (missing java source dir)" | tee "$OUT"; exit 2; }

{
  echo "=== SEARCH: exact expected CSV ==="
  grep -R --include='*.kt' --include='*.java' -n 'app_category_daily.csv' app/src/main/java || echo "[none]"
  echo
  echo "=== SEARCH: likely variants ==="
  grep -R --include='*.kt' --include='*.java' -n -E 'app[_-]?usage[_-]?by[_-]?category.*\.csv' app/src/main/java || echo "[none]"
  grep -R --include='*.kt' --include='*.java' -n -E 'app[_-]?category[_-]?usage.*\.csv' app/src/main/java || echo "[none]"
  grep -R --include='*.kt' --include='*.java' -n -E 'category.*daily.*\.csv' app/src/main/java || echo "[none]"
  grep -R --include='*.kt' --include='*.java' -n -E 'daily.*category.*\.csv' app/src/main/java || echo "[none]"
  echo
  echo "=== SEARCH: directory+filename construction ==="
  grep -R --include='*.kt' --include='*.java' -n -E 'File\(.*,".*category.*\.csv"' app/src/main/java || echo "[none]"
  grep -R --include='*.kt' --include='*.java' -n -E 'ensureHeader\(.*"category.*\.csv"' app/src/main/java || echo "[none]"
  echo
  echo "=== SEARCH: feature identifiers ==="
  grep -R --include='*.kt' --include='*.java' -n -E 'AppUsageByCategory|CategoryDaily|CategoryUsage' app/src/main/java || echo "[none]"
} | tee "$OUT"

echo "GREP-REFS RESULT=PASS" | tee -a "$OUT"
exit 0
