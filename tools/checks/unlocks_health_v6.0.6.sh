#!/bin/sh
OUT="evidence/v6.0/unlocks/health.6.txt"
mkdir -p "$(dirname "$OUT")"
: > "$OUT"
log(){ echo "$*" | tee -a "$OUT"; }

CANDS=$(ls tools/checks/*_v6.0*.sh 2>/dev/null \
 | grep -v '/sleep_' \
 | grep -v 'unlocks_health')
[ -n "$CANDS" ] || { log "UNLOCKS-HEALTH RESULT=FAIL (none)"; exit 2; }

for f in $CANDS; do
  bn=$(basename "$f"); base=${bn%_v6.0.*.sh}; ver=${bn##*_v6.0.}; ver=${ver%.sh}
  eval "cur=\$best_$base"
  [ -z "$cur" ] || [ "$ver" -ge "$cur" ] && { eval "best_$base=$ver"; eval "path_$base=$f"; }
done

SCRIPTS=$(for kv in $(set | grep '^best_' | cut -d= -f1); do key=${kv#best_}; eval "echo \$path_$key"; done)

fail=0; pass=0
for f in $SCRIPTS; do
  t=$(mktemp); sh "$f" >"$t" 2>&1; rc=$?
  sed 's/^/  /' "$t" >>"$OUT"; rm -f "$t"
  [ $rc -eq 0 ] && pass=$((pass+1)) || fail=$((fail+1))
done

{
  echo "SUMMARY: PASS=$pass FAIL=$fail TOTAL=$((pass+fail))"
  [ $fail -eq 0 ] && echo "UNLOCKS-HEALTH RESULT=PASS" || echo "UNLOCKS-HEALTH RESULT=FAIL"
} | tee -a "$OUT"

[ $fail -eq 0 ]
