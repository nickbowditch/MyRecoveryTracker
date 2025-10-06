#!/bin/sh
set -eu
OUT="evidence/v6.0/usage_events/greprefs.txt"
mkdir -p "$(dirname "$OUT")"

fail(){ echo "GREP-REFS RESULT=FAIL $1" | tee "$OUT"; exit 1; }

[ -d app/src/main/java ] || fail "(missing java source dir)"

{
  echo "=== SEARCH: usage_events.csv ==="
  grep -R --include='*.kt' --include='*.java' -n 'usage_events.csv' app/src/main/java || echo "[none]"
  echo
  echo "=== SEARCH: daily_usage_events.csv ==="
  grep -R --include='*.kt' --include='*.java' -n 'daily_usage_events.csv' app/src/main/java || echo "[none]"
  echo
  echo "=== SEARCH: usageEvents ==="
  grep -R --include='*.kt' --include='*.java' -n 'usageEvents' app/src/main/java || echo "[none]"
} | tee "$OUT"

echo "GREP-REFS RESULT=PASS" | tee -a "$OUT"
exit 0
