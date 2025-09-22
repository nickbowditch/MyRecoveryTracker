#!/bin/sh
OUT="evidence/v6.0/unlocks/health.10.txt"
mkdir -p "$(dirname "$OUT")"
: > "$OUT"

CANDS=""
for f in tools/checks/*_v6.0*.sh; do
  case "$f" in */sleep_*|*/git_*|*unlocks_health_*) continue ;; esac
  [ -f "$f" ] || continue
  grep -qE 'daily_unlocks\.csv|unlock_log\.csv|ACTION_RUN_UNLOCK_ROLLUP|unlocks' "$f" && CANDS="$CANDS
$f"
done

[ -n "$CANDS" ] || { echo "UNLOCKS-HEALTH RESULT=FAIL (none)" | tee -a "$OUT"; exit 2; }

for f in $CANDS; do
  bn=$(basename "$f")
  base=${bn%_v6.0.*.sh}
  ver=${bn##*_v6.0.}; ver=${ver%.sh}
  case "$ver" in ''|*[!0-9]*) continue ;; esac
  eval "cur=\${best_$base:-}"
  if [ -z "$cur" ] || [ "$ver" -ge "$cur" ]; then
    eval "best_$base=$ver"
    eval "path_$base='$f'"
  fi
done

SCRIPTS=$(for kv in $(set | awk -F= '/^best_/ {print $1}' | sort); do key=${kv#best_}; eval "echo \${path_$key}"; done)

pass=0; fail=0; failed_list=""
for f in $SCRIPTS; do
  sh "$f" >/dev/null 2>&1; rc=$?
  if [ $rc -eq 0 ]; then pass=$((pass+1)); else fail=$((fail+1)); failed_list="$failed_list $(basename "$f")"; fi
done

{
  echo "SUMMARY: PASS=$pass FAIL=$fail TOTAL=$((pass+fail))"
  if [ $fail -eq 0 ]; then
    echo "UNLOCKS-HEALTH RESULT=PASS"
  else
    echo "UNLOCKS-HEALTH RESULT=FAIL"
    echo "FAILED:$failed_list"
  fi
} | tee -a "$OUT"

[ $fail -eq 0 ]
