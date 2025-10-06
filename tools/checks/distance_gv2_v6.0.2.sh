#!/bin/sh
set -eu
OUT="evidence/v6.0/distance/gv2.txt"
CONSOLE="evidence/v6.0/distance/gv2.console.txt"
mkdir -p "$(dirname "$OUT")"

[ -x ./gradlew ] || { echo "GV2 RESULT=FAIL (no ./gradlew)" | tee "$OUT"; exit 2; }

if ./gradlew :app:qaCheck -q --console=plain >"$CONSOLE" 2>&1; then
  echo "GV2 RESULT=PASS" | tee "$OUT"
  exit 0
fi

echo "GV2 RESULT=FAIL" | tee "$OUT"
exit 1
