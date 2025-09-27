#!/bin/sh
set -eu

OUT="evidence/v6.0/sleep/healthcheck.txt"
mkdir -p "$(dirname "$OUT")"

CHECKS="
ee1_sleep_v6.0.sh
sleep_ee2_v6.0.2.sh
sleep_ee3_v6.0.2.sh
sleep_tc1_v6.0.8.sh
sleep_tc3_v6.0.2.sh
sleep_tc5_v6.0.2.sh
sleep_tc6_v6.0.1.sh
sleep_di1_v6.0.5.sh
sleep_di2_v6.0.2.sh
sleep_di3_v6.0.27.sh
sleep_di4_v6.0.2.sh
sleep_at1_v6.0.4.sh
sleep_at3_v6.0.1.sh
sleep_gv1_v6.0.7.sh
sleep_gv2_v6.0.5.sh
sleep_gv3_v6.0.13.sh
sleep_gv4_v6.0.19.sh
sleep_gv5_v6.0.1.sh
sleep_gv6_v6.0.1.sh
sleep_gv7_v6.0.3.sh
"

RESULTS=""

for chk in $CHECKS; do
script="tools/checks/$chk"
if [ ! -x "$script" ]; then
RESULTS="${RESULTS}${chk}: MISSING\n"
continue
fi
OUTTXT="$(mktemp)"
if "$script" >"$OUTTXT" 2>&1; then
res="$(grep -Eo '[A-Z0-9-]+ RESULT[:=][^ ]* PASS' "$OUTTXT" | tail -n1 || true)"
[ -n "$res" ] || res="$chk: PASS"
else
res="$(grep -Eo '[A-Z0-9-]+ RESULT[:=][^ ]* FAIL.*' "$OUTTXT" | tail -n1 || true)"
[ -n "$res" ] || res="$chk: FAIL"
fi
RESULTS="${RESULTS}${res}\n"
rm -f "$OUTTXT"
done

printf "%b" "$RESULTS" | tee "$OUT"
