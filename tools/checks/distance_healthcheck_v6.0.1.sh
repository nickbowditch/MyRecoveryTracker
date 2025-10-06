#!/bin/sh
set -eu
OUT="evidence/v6.0/distance/healthcheck.txt"
mkdir -p "$(dirname "$OUT")"

CHECKS="
ee1_location_permission_check.sh
ee2_workers_scheduled_triggerable.sh
ee3_runs_under_doze_idle.sh
ee4_evidence_whitelisted.sh
distance_tc1_v6.0.sh
distance_tc2_v6.0.sh
distance_tc3_v6.0.sh
distance_tc5_v6.0.sh
distance_tc6_v6.0.sh
distance_di1_v6.0.sh
distance_di2_v6.0.sh
distance_di3_v6.0.sh
distance_di4_v6.0.sh
distance_at1_v6.0.sh
distance_at2_v6.0.sh
distance_at3_v6.0.sh
distance_gv1_v6.0.sh
distance_gv2_v6.0.2.sh
distance_gv3_v6.0.sh
distance_gv4_v6.0.sh
distance_gv5_v6.0.sh
distance_gv6_v6.0.sh
distance_gv7_v6.0.sh
"

PASS_RE='[A-Z0-9-]+[[:space:]]+RESULT[[:space:]][:=][[:space:]]PASS([[:space:]]|$)'
FAIL_RE='[A-Z0-9-]+[[:space:]]+RESULT[[:space:]][:=][[:space:]]FAIL([[:space:]]|$)'

RESULTS=""
PASS_N=0
FAIL_N=0
MISS_N=0

for chk in $CHECKS; do
  script="tools/checks/$chk"
  if [ ! -x "$script" ]; then
    RESULTS="${RESULTS}${chk}: MISSING\n"
    MISS_N=$((MISS_N+1))
    continue
  fi
  OUTTXT="$(mktemp)"
  if "$script" >"$OUTTXT" 2>&1; then
    msg="$(grep -Eo "$PASS_RE" "$OUTTXT" | tail -n1 || true)"
    [ -n "$msg" ] || msg="$chk: PASS"
    PASS_N=$((PASS_N+1))
  else
    msg="$(grep -Eo "$FAIL_RE" "$OUTTXT" | tail -n1 || true)"
    [ -n "$msg" ] || msg="$chk: FAIL"
    FAIL_N=$((FAIL_N+1))
  fi
  RESULTS="${RESULTS}${msg}\n"
  rm -f "$OUTTXT"
done

TOTAL=$((PASS_N+FAIL_N+MISS_N))
{
  echo "HEALTHCHECK Distance v6.0"
  echo "total=$TOTAL pass=$PASS_N fail=$FAIL_N missing=$MISS_N"
  printf "%b" "$RESULTS"
} | tee "$OUT"
