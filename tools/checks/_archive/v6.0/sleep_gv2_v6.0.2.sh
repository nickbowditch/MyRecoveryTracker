#!/bin/bash
OUT="evidence/v6.0/sleep/gv2.2.txt"
[ -x ./gradlew ] || { echo "GV-2 RESULT=FAIL (no ./gradlew)"; exit 2; }
./gradlew -q :app:tasks --all | grep -qE '(^|:)qaCheck$' || { echo "GV-2 RESULT=FAIL (qaCheck task missing)"; exit 3; }
SRC="$(cat app/build.gradle.kts 2>/dev/null; cat qa-core.gradle.kts 2>/dev/null; cat build.gradle.kts 2>/dev/null)"
printf '%s' "$SRC" | grep -q 'qaCheck' || { echo "GV-2 RESULT=FAIL (qaCheck def missing)"; exit 4; }
printf '%s' "$SRC" | grep -q 'tools/checks/unlocks_' || { echo "GV-2 RESULT=FAIL (unlocks checks not included)"; exit 5; }
printf '%s' "$SRC" | grep -q 'tools/checks/sleep_' || { echo "GV-2 RESULT=FAIL (sleep checks not included)"; exit 6; }
./gradlew -m -q :app:qaCheck >/dev/null 2>&1 || { echo "GV-2 RESULT=FAIL (qaCheck dry-run failed)"; exit 7; }
echo "GV-2 RESULT=PASS" | tee "$OUT"; exit 0
