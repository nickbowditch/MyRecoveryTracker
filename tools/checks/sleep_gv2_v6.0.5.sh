#!/bin/bash
OUT="evidence/v6.0/sleep/gv2.5.txt"
[ -x ./gradlew ] || { echo "GV-2 RESULT=FAIL (no ./gradlew)" | tee "$OUT"; exit 2; }
./gradlew -m -q :app:qaCheck >/dev/null 2>&1 || { echo "GV-2 RESULT=FAIL (qaCheck dry-run failed)" | tee "$OUT"; exit 3; }
echo "GV-2 RESULT=PASS" | tee "$OUT"; exit 0
