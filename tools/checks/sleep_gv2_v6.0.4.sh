#!/bin/bash
OUT="evidence/v6.0/sleep/gv2.4.txt"
[ -x ./gradlew ] || { echo "GV-2 RESULT=FAIL (no ./gradlew)"; exit 2; }
./gradlew -m -q :app:qaCheck >/dev/null 2>&1 || { echo "GV-2 RESULT=FAIL (qaCheck dry-run failed)"; exit 3; }
echo "GV-2 RESULT=PASS" | tee "$OUT"; exit 0
