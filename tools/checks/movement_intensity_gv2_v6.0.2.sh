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

echo "--- DEBUG: movement_intensity scripts present ---"
SCRIPTS_FOUND="$(ls tools/checks/movement_intensity_*.sh 2>/dev/null || true)"
[ -n "$SCRIPTS_FOUND" ] && printf '%s\n' "$SCRIPTS_FOUND" || echo "<none>"

echo "--- DEBUG: Gradle :app:qaCheck --dry-run (first 300 lines) ---"
GRADLE_OUT="$(./gradlew :app:qaCheck --dry-run --console plain 2>&1 || true)"
printf '%s\n' "$GRADLE_OUT" | sed -n '1,300p'

[ -n "$GRADLE_OUT" ] || fail "Gradle dry-run produced no output"

MATCH_LINES="$(printf '%s\n' "$GRADLE_OUT" | { grep -Ei 'movement[_-]?intensity|tools/checks/movement_intensity_.*\.sh|movementIntensity' || true; })"
HIT_COUNT="$(printf '%s\n' "$MATCH_LINES" | awk 'NF{c++} END{print c+0}')"
echo "matches_in_dry_run=$HIT_COUNT"

if [ "$HIT_COUNT" -gt 0 ]; then
  echo "--- DEBUG: matching lines ---"
  printf '%s\n' "$MATCH_LINES" | sed -n '1,50p'
  echo "GV2 RESULT=PASS"
  exit 0
fi

echo "--- DEBUG: :app:tasks --all (first 300 lines) ---"
TASKS_OUT="$(./gradlew :app:tasks --all --console plain 2>&1 || true)"
printf '%s\n' "$TASKS_OUT" | sed -n '1,300p'

QA_PRESENT="$(printf '%s\n' "$TASKS_OUT" | { grep -E '^qaCheck|:app:qaCheck' || true; } | wc -l | tr -d ' ')"
echo "qaCheck_task_present=$QA_PRESENT"

echo "--- DEBUG: probing task graph with --info ---"
INFO_OUT="$(./gradlew :app:qaCheck --dry-run --info --console plain 2>&1 || true)"
printf '%s\n' "$INFO_OUT" | sed -n '1,200p'

GRAPH_MATCHES="$(printf '%s\n' "$INFO_OUT" | { grep -Ei 'movement[_-]?intensity|tools/checks/movement_intensity_.*\.sh|movementIntensity' || true; })"
GRAPH_HIT_COUNT="$(printf '%s\n' "$GRAPH_MATCHES" | awk 'NF{c++} END{print c+0}')"
echo "matches_in_task_graph=$GRAPH_HIT_COUNT"
[ "$GRAPH_HIT_COUNT" -gt 0 ] && { printf '%s\n' "$GRAPH_MATCHES" | sed -n '1,50p'; echo "GV2 RESULT=PASS"; exit 0; }

fail "movement_intensity checks not wired into :app:qaCheck"
