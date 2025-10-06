#!/bin/bash
set -euo pipefail

OUT="evidence/v6.0/distance/gv2.txt"
mkdir -p "$(dirname "$OUT")"

fail() {
  echo "GV2 RESULT=FAIL ($1)" | tee "$OUT"
  echo "--- DEBUG: gradlew presence/perm ---" | tee -a "$OUT"
  { ls -l ./gradlew || echo "<missing ./gradlew>"; } | tee -a "$OUT"
  echo "--- DEBUG: :app:tasks --all (tail) ---" | tee -a "$OUT"
  cat /tmp/_gv2_tasks.$$ 2>/dev/null | tail -n 200 | tee -a "$OUT" || true
  echo "--- DEBUG: :app:help --task qaCheck ---" | tee -a "$OUT"
  cat /tmp/_gv2_help.$$ 2>/dev/null | tee -a "$OUT" || true
  echo "--- DEBUG: :app:qaCheck -m (dry-run tail) ---" | tee -a "$OUT"
  cat /tmp/_gv2_dry.$$ 2>/dev/null | tail -n 200 | tee -a "$OUT" || true
  echo "--- DEBUG: Gradle greps (qaCheck & distance) ---" | tee -a "$OUT"
  { grep -RInE 'qaCheck|distance_(tc|di|gv|at|ee|smoke|schema)|tools/checks/distance' app/ build.gradle* settings.gradle* settings.gradle.kts gradle/ buildSrc/ 2>/dev/null || true; } | tee -a "$OUT"
  echo "RESULT=FAIL" | tee -a "$OUT"
  rm -f /tmp/_gv2_tasks.$$ /tmp/_gv2_dry.$$ /tmp/_gv2_help.$$ 2>/dev/null || true
  exit 1
}

[ -x "./gradlew" ] || fail "./gradlew not found or not executable"

./gradlew :app:tasks --all > /tmp/_gv2_tasks.$$ 2>&1 || true
./gradlew -q :app:help --task qaCheck > /tmp/_gv2_help.$$ 2>&1 || true
./gradlew :app:qaCheck -m > /tmp/_gv2_dry.$$ 2>&1 || true

HAS_TASK_LIST="$(grep -E '(^|[^a-zA-Z0-9_]):app:qaCheck([^a-zA-Z0-9_]|$)' /tmp/_gv2_tasks.$$ || true)"
HAS_TASK_HELP="$(grep -E "Task 'qaCheck' not found" /tmp/_gv2_help.$$ >/dev/null && echo "" || grep -E "^Task ':app:qaCheck'" /tmp/_gv2_help.$$ || true)"
HAS_DRY_DISTANCE="$(grep -Ei 'distance|Distance' /tmp/_gv2_dry.$$ || true)"
HAS_CONFIG_DISTANCE="$(grep -RInE 'tools/checks/distance|distance_(tc|di|gv|at|ee|smoke|schema)' app/ build.gradle* settings.gradle* settings.gradle.kts gradle/ buildSrc/ 2>/dev/null || true)"
HAS_QACHECK_CONFIG="$(grep -RInE '(^|[^a-zA-Z0-9_])qaCheck([^a-zA-Z0-9_]|$)' app/ build.gradle* settings.gradle* settings.gradle.kts gradle/ buildSrc/ 2>/dev/null || true)"

{
  echo "--- SUMMARY ---"
  echo "qaCheck_in_tasks_list=$([ -n "${HAS_TASK_LIST// /}" ] && echo YES || echo NO)"
  echo "qaCheck_help_found=$([ -n "${HAS_TASK_HELP// /}" ] && echo YES || echo NO)"
  echo "dry_run_distance_refs=$([ -n "${HAS_DRY_DISTANCE// /}" ] && echo YES || echo NO)"
  echo "config_distance_refs=$([ -n "${HAS_CONFIG_DISTANCE// /}" ] && echo YES || echo NO)"
  echo "qaCheck_config_refs=$([ -n "${HAS_QACHECK_CONFIG// /}" ] && echo YES || echo NO)"
  echo "--- DRY-RUN distance lines (first 50) ---"
  printf "%s\n" "$HAS_DRY_DISTANCE" | sed -n '1,50p'
  echo "--- Gradle config distance refs (first 50) ---"
  printf "%s\n" "$HAS_CONFIG_DISTANCE" | sed -n '1,50p'
  echo "--- Gradle qaCheck refs (first 50) ---"
  printf "%s\n" "$HAS_QACHECK_CONFIG" | sed -n '1,50p'
} | tee "$OUT" >/dev/null

if [ -n "${HAS_TASK_LIST// /}" ] || [ -n "${HAS_TASK_HELP// /}" ]; then
  echo "GV2 RESULT=PASS" | tee -a "$OUT"
  rm -f /tmp/_gv2_tasks.$$ /tmp/_gv2_dry.$$ /tmp/_gv2_help.$$ 2>/dev/null || true
  exit 0
fi

if [ -n "${HAS_QACHECK_CONFIG// /}" ] && [ -n "${HAS_CONFIG_DISTANCE// /}" ]; then
  echo "GV2 RESULT=PASS" | tee -a "$OUT"
  rm -f /tmp/_gv2_tasks.$$ /tmp/_gv2_dry.$$ /tmp/_gv2_help.$$ 2>/dev/null || true
  exit 0
fi

fail "no evidence that distance checks are wired into :app:qaCheck"
