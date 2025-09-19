#!/usr/bin/env bash
set -euo pipefail
./gradlew -q tasks --all | grep -q "^app:qaCheck " || { echo "Sleep GV-2 RESULT=FAIL (no :app:qaCheck)"; exit 1; }
./gradlew :app:qaCheck >/dev/null 2>&1 || { echo "Sleep GV-2 RESULT=FAIL (qaCheck failed)"; exit 1; }
echo "Sleep GV-2 RESULT=PASS"
