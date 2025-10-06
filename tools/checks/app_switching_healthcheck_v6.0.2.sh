#!/bin/sh
set -eu
OUT="evidence/v6.0/app_switching/healthcheck.txt"
mkdir -p "$(dirname "$OUT")"

CHECKS="
app_switching_ee1_v6.0.1.sh
app_switching_ee2_v6.0.6.sh
app_switching_ee3_v6.0.sh
app_switching_ee4_v6.0.2.sh
app_switching_tc1_v6.0.1.sh
app_switching_tc2_v6.0.1.sh
app_switching_tc3_v6.0.sh
app_switching_tc5_v6.0.sh
app_switching_tc6_v6.0.sh
app_switching_di1_v6.0.sh
app_switching_di2_v6.0.sh
app_switching_di3_v6.0.2.sh
app_switching_di4_v6.0.sh
app_switching_at1_v6.0.sh
app_switching_at2_v6.0.5.sh
app_switching_at3_v6.0.sh
app_switching_gv1_v6.0.1.sh
app_switching_gv2_v6.0.1.sh
app_switching_gv3_v6.0.1.sh
app_switching_gv4_v6.0.1.sh
app_switching_gv5_v6.0.1.sh
app_switching_gv6_v6.0.2.sh
app_switching_gv7_v6.0.1.sh
"

PASS_RE='[A-Z0-9-]+[[:space:]]+RESULT[[:space:]][:=][[:space:]]PASS(\b|$)'
FAIL_RE='[A-Z0-9-]+[[:space:]]+RESULT[[:space:]][:=][[:space:]]FAIL(\b|$)'

RESULTS=""
for chk in $CHECKS; do
  script="tools/checks/$chk"
  if [ ! -x "$script" ]; then
    RESULTS="${RESULTS}${chk}: MISSING\n"
    continue
  fi
  OUTTXT="$(mktemp)"
  if "$script" >"$OUTTXT" 2>&1; then
    msg="$(grep -Eo "$PASS_RE" "$OUTTXT" | tail -n1 || true)"
    [ -n "$msg" ] || msg="$chk: PASS"
  else
    msg="$(grep -Eo "$FAIL_RE" "$OUTTXT" | tail -n1 || true)"
    [ -n "$msg" ] || msg="$chk: FAIL"
  fi
  RESULTS="${RESULTS}${msg}\n"
  rm -f "$OUTTXT"
done

printf "%b" "$RESULTS" | tee "$OUT"
