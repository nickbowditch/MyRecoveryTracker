#!/bin/bash
set -euo pipefail
PKG="com.nick.myrecoverytracker"
EVDIR="evidence/v6.0/movement_intensity"
OUT="$EVDIR/gv2.txt"

mkdir -p "$(dirname "$OUT")" "$EVDIR"
exec > >(tee "$OUT") 2>&1
fail(){ echo "GV2 RESULT=FAIL ($1)"; exit 1; }

adb get-state >/dev/null 2>&1 || { echo "GV2 RESULT=FAIL (no device)"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "GV2 RESULT=FAIL (app not installed)"; exit 3; }

echo "started_at=$(date -u +'%Y-%m-%dT%H:%M:%SZ')"
echo "pwd=$(pwd)"

SCRIPTS_FOUND="$(ls tools/checks/movement_intensity_*.sh 2>/dev/null || true)"
echo "--- DEBUG: movement_intensity scripts present ---"
[ -n "$SCRIPTS_FOUND" ] && printf '%s\n' "$SCRIPTS_FOUND" || echo "<none>"

echo "--- DEBUG: Gradle :app:qaCheck --dry-run ---"
GRADLE_OUT="$(./gradlew -q :app:qaCheck --dry-run 2>&1 || true)"
echo "$GRADLE_OUT" | sed -n '1,200p'

[ -n "$GRADLE_OUT" ] || fail "Gradle dry-run produced no output"

HIT_COUNT="$(printf '%s\n' "$GRADLE_OUT" \
  | grep -E -i 'movement[_-]?intensity|tools/checks/movement_intensity_.*\.sh|movementIntensity' \
  | wc -l | tr -d ' ')"

echo "matches_in_dry_run=$HIT_COUNT"

if [ "$HIT_COUNT" -gt 0 ]; then
  echo "--- DEBUG: matching lines ---"
  printf '%s\n' "$GRADLE_OUT" \
    | grep -E -i 'movement[_-]?intensity|tools/checks/movement_intensity_.*\.sh|movementIntensity' \
    | head -n 50
  echo "GV2 RESULT=PASS"
  exit 0
fi

echo "--- DEBUG: :app:tasks --all (first 200 lines) ---"
TASKS_OUT="$(./gradlew -q :app:tasks --all 2>&1 || true)"
echo "$TASKS_OUT" | sed -n '1,200p'
TASK_HITS="$(printf '%s\n' "$TASKS_OUT" | grep -E -i 'qa(check|task)|movement[_-]?intensity|movementIntensity' | wc -l | tr -d ' ')"
echo "matches_in_tasks=$TASK_HITS"

fail "movement_intensity checks not wired into :app:qaCheck"
