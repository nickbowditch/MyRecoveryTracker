#!/usr/bin/env bash
set -euo pipefail
./gradlew -q tasks --all | grep -q "^app:qaVerifyMetrics " || { echo "Sleep GV-4 RESULT=FAIL (no :app:qaVerifyMetrics)"; exit 1; }
./gradlew :app:qaVerifyMetrics --no-daemon --console=plain >/dev/null 2>&1 || { echo "Sleep GV-4 RESULT=FAIL (qaVerifyMetrics failed)"; exit 1; }
echo "Sleep GV-4 RESULT=PASS"
