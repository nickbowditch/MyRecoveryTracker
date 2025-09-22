#!/bin/sh
OUT="evidence/v6.0/unlocks/health.8.txt"
mkdir -p "$(dirname "$OUT")"
: > "$OUT"

CANDS=$(ls tools/checks/*_v6.0*.sh 2>/dev/null \
 | grep -v '/sleep_' \
 | grep -v '/git_' \
 | grep -v '/unlocks_health_' \
 | xargs grep -l -E 'daily_unlocks\.csv|unlock_log\.csv|ACTION_RUN_UNLOCK_ROLLUP|unlocks' 2>/dev/null | sort -u)

[ -n "$CANDS" ] || { echo "UNLOCKS-HEALTH RESULT=FAIL (none)" | tee -a "$OUT"; exit 2; }

for f in $CANDS; do
  bn=$(basename "$f")
  base=${bn%_v6.0.*.sh}
  ver=${bn##*_v6.0.}; ver=${ver%.sh}
  case $ver in ''|*[!0-9]*) continue ;; esac
  eval "cur=\$best_$base"
  if [ -z "$cur" ] || [ "$ver" -ge "$cur" ]; then
    eval "best_$base=$ver"
    eval "path_$base='$f'"
  fi
done

SCRIPTS=$(for kv in $(set | grep '^best_' | cut -d= -f1 | sort); do key=${kv#best_}; eval "echo \$path_$key"; done)

pass=0; fail=0; failed_list=""
for f in $SCRIPTS; do
  b=$(basename "$f")
  echo "CHOSEN:$b" | tee -a "$OUT" >/dev/null
  sh "$f" >/dev/null 2>&1
  rc=$?
  if [ $rc -eq 0 ]; then pass=$((pass+1)); else fail=$((fail+1)); failed_list="$failed_list $b"; fi
done

{
  echo "SUMMARY: PASS=$pass FAIL=$fail TOTAL=$((pass+fail))"
  if [ $fail -eq 0 ]; then
    echo "UNLOCKS-HEALTH RESULT=PASS"
  else
    echo "UNLOCKS-HEALTH RESULT=FAIL"
    echo "FAILED:$failed_list"
  fi
} | tee -a "$OUT" >/dev/null

[ $fail -eq 0 ]
