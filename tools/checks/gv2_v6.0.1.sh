#!/bin/bash
./gradlew tasks --all | grep -q "qaCheck" || { echo "GV-2 RESULT=FAIL (no qaCheck task)"; exit 1; }
./gradlew :app:qaCheck >/dev/null 2>&1 || { echo "GV-2 RESULT=FAIL (:app:qaCheck failed)"; exit 2; }
echo "GV-2 RESULT=PASS"
exit 0
