#!/bin/sh
OUT="evidence/v6.0/unlocks/health.11.txt"
mkdir -p "$(dirname "$OUT")"; : > "$OUT"
rec="./tools/fixtures/unlocks_reconcile_v6.0.2.sh"

pick_latest(){
  CANDS="$1"
  for f in $CANDS; do
    bn=$(basename "$f"); base=${bn%_v6.0.*.sh}; ver=${bn##*_v6.0.}; ver=${ver%.sh}
    case "$ver" in ''|*[!0-9]*) continue;; esac
    eval "cur=\${best_$base:-}"
    if [ -z "$cur" ] || [ "$ver" -ge "$cur" ]; then
      eval "best_$base=$ver"; eval "path_$base='$f'"
    fi
  done
  for kv in $(set | awk -F= '/^best_/ {print $1}' | sort); do key=${kv#best_}; eval "echo \${path_$key}"; done
}

all=$(for f in tools/checks/*_v6.0*.sh; do
  case "$f" in */sleep_*|*/git_*|*unlocks_health_*) continue;; esac
  [ -f "$f" ] || continue
  grep -qE 'daily_unlocks\.csv|unlock_log\.csv|ACTION_RUN_UNLOCK_ROLLUP|unlocks' "$f" && echo "$f"
done)

tc3=$(echo "$all" | grep '/tc3_v6.0' | sort -V | tail -n1)
mut=$(echo "$all" | grep -E '/(at1|at2|tc2|tc4|unlocks_tc4)_v6.0' | pick_latest "$(cat)")

pass=0; fail=0; failed=""
run(){ sh "$1" >/dev/null 2>&1; rc=$?; [ $rc -eq 0 ] && pass=$((pass+1)) || { fail=$((fail+1)); failed="$failed $(basename "$1")"; }; }

[ -n "$tc3" ] && run "$tc3"
for f in $rest; do run "$f"; done
for f in $mut; do run "$f"; done
[ -x "$rec" ] && "$rec" >/dev/null 2>&1 || true

{
  echo "SUMMARY: PASS=$pass FAIL=$fail TOTAL=$((pass+fail))"
  [ $fail -eq 0 ] && echo "UNLOCKS-HEALTH RESULT=PASS" || { echo "UNLOCKS-HEALTH RESULT=FAIL"; echo "FAILED:$failed"; }
} | tee -a "$OUT"

[ $fail -eq 0 ]
