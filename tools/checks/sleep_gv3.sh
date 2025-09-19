#!/usr/bin/env bash
set -euo pipefail
./gradlew -q tasks --all | grep -q "^app:writerGuardUnlocks " || { echo "Sleep GV-3 RESULT=FAIL (no :app:writerGuardUnlocks)"; exit 1; }
./gradlew :app:writerGuardUnlocks >/dev/null 2>&1 || { echo "Sleep GV-3 RESULT=FAIL (writerGuardUnlocks failed)"; exit 1; }
echo "Sleep GV-3 RESULT=PASS"
