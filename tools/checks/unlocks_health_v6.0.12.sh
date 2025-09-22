#!/bin/sh
OUT="evidence/v6.0/unlocks/health.12.txt"
mkdir -p "$(dirname "$OUT")"; : > "$OUT"

CANDS=""
for f in tools/checks/*_v6.0*.sh; do
  case "$f" in */sleep_*|*/git_*|*unlocks_health_*) continue;; esac
  [ -f "$f" ] || continue
  grep -qE 'daily_unlocks\.csv|unlock_log\.csv|ACTION_RUN_UNLOCK_ROLLUP|unlocks' "$f" || continue
  CANDS="$CANDS
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

SCRIPTS=""
for kv in $(set | awk -F= '/^best_/ {print $1}' | sort); do
  key=${kv#best_}; eval "pf=\${path_$key:-}"
  [ -n "$pf" ] && SCRIPTS="$SCRIPTS
$pf"
done

tc3=""; rest=""; mut=""
for f in $SCRIPTS; do
  b=$(basename "$f")
  case "$b" in
    tc3_v6.0*.sh) tc3="$f" ;;
$f" ;;
    *) rest="$rest
$f" ;;
  esac
done

run_one(){ sh "$1" >/dev/null 2>&1; return $?; }

pass=0; fail=0; failed=""
[ -n "$tc3" ] && { run_one "$tc3"; rc=$?; [ $rc -eq 0 ] && pass=$((pass+1)) || { fail=$((fail+1)); failed="$failed $(basename "$tc3")"; }; }
for f in $rest; do [ -n "$f" ] || continue; run_one "$f"; rc=$?; [ $rc -eq 0 ] && pass=$((pass+1)) || { fail=$((fail+1)); failed="$failed $(basename "$f")"; }; done
for f in $mut;  do [ -n "$f" ] || continue; run_one "$f"; rc=$?; [ $rc -eq 0 ] && pass=$((pass+1)) || { fail=$((fail+1)); failed="$failed $(basename "$f")"; }; done

[ -x tools/fixtures/unlocks_reconcile_v6.0.2.sh ] && tools/fixtures/unlocks_reconcile_v6.0.2.sh >/dev/null 2>&1 || true

echo "SUMMARY: PASS=$pass FAIL=$fail TOTAL=$((pass+fail))" | tee -a "$OUT"
if [ $fail -eq 0 ]; then
  echo "UNLOCKS-HEALTH RESULT=PASS" | tee -a "$OUT"
  exit 0
else
  echo "UNLOCKS-HEALTH RESULT=FAIL" | tee -a "$OUT"
  echo "FAILED:$failed" | tee -a "$OUT"
  exit 1
fi
