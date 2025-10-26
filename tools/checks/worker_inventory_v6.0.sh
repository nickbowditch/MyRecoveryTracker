#!/bin/bash
set -euo pipefail
OUT="evidence/v6.0/_repo/worker_inventory.txt"
SRC="app/src"
mkdir -p "$(dirname "$OUT")"
exec > >(tee "$OUT") 2>&1

echo "WORKER_INVENTORY v6.0"
grep -Rho 'class [A-Za-z0-9_]*Worker' "$SRC" | awk '{print $2}' | sort -u > /tmp/declared.txt
find "$SRC" -type f -name '*Worker.kt' -exec basename {} \; | sed 's/.kt$//' | sort -u > /tmp/files.txt

echo "DECLARED:"
cat /tmp/declared.txt
echo
echo "FILES:"
cat /tmp/files.txt
echo
echo "MISSING_FILES:"
comm -23 /tmp/declared.txt /tmp/files.txt || true
echo
echo "UNUSED_FILES:"
comm -13 /tmp/declared.txt /tmp/files.txt || true
