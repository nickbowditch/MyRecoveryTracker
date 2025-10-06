#!/bin/sh
set -eu
OUT="evidence/v6.0/usage_events/healthcheck.txt"
mkdir -p "$(dirname "$OUT")"

CHECKS="
usage_events_ee1_v6.0.sh
usage_events_ee2_v6.0.sh
usage_events_ee3_v6.0.sh
usage_events_ee4_v6.0.1.sh
usage_events_tc1_v6.0.1.sh
usage_events_tc2_v6.0.2.sh
usage_events_tc3_v6.0.1.sh
usage_events_tc5_v6.0.1.sh
usage_events_tc6_v6.0.1.sh
usage_events_di1_v6.0.1.sh
usage_events_di2_v6.0.2.sh
usage_events_di3_v6.0.2.sh
usage_events_di4_v6.0.1.sh
usage_events_at1_v6.0.4.sh
usage_events_at2_rerun_v6.0.2.sh
usage_events_at3_v6.0.1.sh
usage_events_gv1_v6.0.1.sh
usage_events_gv2_v6.0.1.sh
usage_events_gv3_v6.0.1.sh
usage_events_gv4_v6.0.1.sh
usage_events_gv5_v6.0.1.sh
usage_events_gv6_v6.0.1.sh
usage_events_gv7_v6.0.1.sh
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
