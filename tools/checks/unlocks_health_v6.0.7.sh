#!/bin/sh
OUT="evidence/v6.0/unlocks/health.7.txt"
mkdir -p "$(dirname "$OUT")"
: > "$OUT"

CANDS=$(ls tools/checks/*_v6.0*.sh 2>/dev/null \
 | grep -v '/sleep_' \
 | grep -v 'unlocks_health')
[ -n "$CANDS" ] || { echo "UNLOCKS-HEALTH RESULT=FAIL (none)" | tee -a "$OUT"; exit 2; }

for f in $CANDS; do
  bn=$(basename "$f")
  base=${bn%_v6.0.*.sh}
  ver=${bn##*_v6.0.}; ver=${ver%.sh}
  case $ver in
    ''|*[!0-9]*) continue ;; # skip if not integer
  esac
  eval "cur=\$best_$base"
  [ -z "$cur" ] || [ "$ver" -ge "$cur" ] && { eval "best_$base=$ver"; eval "path_$base='$f'"; }
done

SCRIPTS=$(for kv in $(set | grep '^best_' | cut -d= -f1); do key=${kv#best_}; eval "echo \$path_$key"; done)

fail=0; pass=0; failed_list=""
for f in $SCRIPTS; do
  t=$(mktemp); sh "$f" >"$t" 2>&1; rc=$?
  sed 's/^/  /' "$t" >>"$OUT"; rm -f "$t"
  if [ $rc -eq 0 ]; then pass=$((pass+1)); else fail=$((fail+1)); failed_list="$failed_list $(basename "$f")"; fi
done

{
  echo "SUMMARY: PASS=$pass FAIL=$fail TOTAL=$((pass+fail))"
  [ $fail -eq 0 ] && echo "UNLOCKS-HEALTH RESULT=PASS" || {
    echo "UNLOCKS-HEALTH RESULT=FAIL"
    echo "FAILED:$failed_list"
  }
} | tee -a "$OUT"

[ $fail -eq 0 ]
