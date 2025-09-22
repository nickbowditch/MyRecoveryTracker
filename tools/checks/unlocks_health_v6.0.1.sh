#!/bin/sh
OUT="evidence/v6.0/unlocks/health.1.txt"
mkdir -p "$(dirname "$OUT")"
: > "$OUT"

log(){ printf '%s\n' "$*" | tee -a "$OUT"; }

SCRIPTS="$(ls tools/checks/*_v6.0*.sh 2>/dev/null | grep -v '/sleep_' | xargs grep -l -E 'daily_unlocks\.csv|unlock_log\.csv|ACTION_RUN_UNLOCK_ROLLUP|unlocks' 2>/dev/null | sort -u)"

[ -n "$SCRIPTS" ] || { log "UNLOCKS-HEALTH RESULT=FAIL (no unlocks checks found)"; exit 2; }

fail=0
for f in $SCRIPTS; do
  b="$(basename "$f")"
  log "RUN:$b"
  t="$(mktemp)"
  sh "$f" >"$t" 2>&1
  rc=$?
  sed 's/^/  /' "$t" | tee -a "$OUT" >/dev/null
  rm -f "$t"
  if [ $rc -eq 0 ]; then
    log "CHECK:$b RESULT=PASS"
  else
    log "CHECK:$b RESULT=FAIL (rc=$rc)"
    fail=$((fail+1))
  fi
done

if [ $fail -eq 0 ]; then
  log "UNLOCKS-HEALTH RESULT=PASS"
  exit 0
else
  log "UNLOCKS-HEALTH RESULT=FAIL ($fail failing checks)"
  exit 1
fi
