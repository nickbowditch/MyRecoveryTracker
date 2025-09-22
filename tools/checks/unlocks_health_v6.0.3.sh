#!/bin/sh
OUT="evidence/v6.0/unlocks/health.3.txt"
mkdir -p "$(dirname "$OUT")"
: > "$OUT"
log(){ printf '%s\n' "$*" | tee -a "$OUT"; }

CANDS="$(ls tools/checks/*_v6.0*.sh 2>/dev/null \
 | grep -v '/sleep_' | grep -v '/unlocks_health_' \
 | xargs grep -l -E 'daily_unlocks\.csv|unlock_log\.csv|ACTION_RUN_UNLOCK_ROLLUP|unlocks' 2>/dev/null | sort -u)"

[ -n "$CANDS" ] || { log "UNLOCKS-HEALTH RESULT=FAIL (no unlocks checks found)"; exit 2; }

for f in $CANDS; do
  bn="$(basename "$f")"
  base="${bn%_v6.0.*.sh}"
  ver="${bn##*_v6.0.}"; ver="${ver%.sh}"
  cur="$(eval "printf %s \"\$best_$base\" 2>/dev/null")"
  if [ -z "$cur" ] || [ "$ver" -ge "$cur" ]; then
    eval "best_$base=$ver"
    eval "path_$base='$f'"
  fi
done

SCRIPTS=""
for kv in $(set | grep '^best_' | cut -d= -f1); do
  key="${kv#best_}"
  eval "pf=\$path_$key"
  [ -n "$pf" ] && SCRIPTS="$SCRIPTS
$pf"
done

fail=0
for f in $SCRIPTS; do
  b="$(basename "$f")"
  log "RUN:$b"
  t="$(mktemp)"; sh "$f" >"$t" 2>&1; rc=$?
  sed 's/^/  /' "$t" | tee -a "$OUT" >/dev/null; rm -f "$t"
  [ $rc -eq 0 ] && log "CHECK:$b RESULT=PASS" || { log "CHECK:$b RESULT=FAIL (rc=$rc)"; fail=$((fail+1)); }
done

[ $fail -eq 0 ] && { log "UNLOCKS-HEALTH RESULT=PASS"; exit 0; } || { log "UNLOCKS-HEALTH RESULT=FAIL ($fail failing checks)"; exit 1; }
