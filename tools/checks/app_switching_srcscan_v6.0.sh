#!/bin/sh
set -eu
OUT="evidence/v6.0/app_switching/srcscan.txt"
mkdir -p "$(dirname "$OUT")"

echo "=== SOURCE: writers that touch daily_app_switching.csv ==="
grep -R --include='*.kt' --include='*.java' -nE 'File\(.*"daily_app_switching\.csv"' app/src/main/java || true
echo

echo "=== SOURCE: lines that set header to date,switches,entropy (rollup) ==="
grep -R --include='*.kt' --include='*.java' -nE 'ensureHeader\(.*"date,switches,entropy"|writeText\("date,switches,entropy' app/src/main/java || true
echo

echo "=== SOURCE: lines that set header to date,package,starts (per-package) ==="
grep -R --include='*.kt' --include='*.java' -nE 'ensureHeader\(.*"date,package,starts"|writeText\("date,package,starts' app/src/main/java || true
echo

echo "=== SOURCE: UsageCaptureWorker context around per-package writes ==="
f="$(git ls-files 'app/src/main/java/**/UsageCaptureWorker.kt' | head -n1 || true)"
if [ -n "$f" ]; then
  nl -ba "$f" | sed -n '1,260p' | grep -nE 'ensureHeader|daily_app_switching|package,starts|date,count' || true
else
  echo "[UsageCaptureWorker.kt not found]"
fi
