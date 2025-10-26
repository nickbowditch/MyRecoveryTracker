#!/bin/bash
set -eu

OUT="evidence/v6.0/unlocks/healthcheck.txt"
mkdir -p "$(dirname "$OUT")"

CHECKS="
ee1_v6.0.sh
ee2_v6.0.1.sh
ee3_v6.0.1.sh
unlocks_ee4_v6.0.1.sh
tc1_v6.0.3.sh
tc2_v6.0.24.sh
tc3_v6.0.1.sh
tc5_v6.0.1.sh
unlocks_tc6_v6.0.1.sh
unlocks_di1_v6.0.8.sh
unlocks_di2_v6.0.2.sh
unlocks_di3_v6.0.4.sh
unlocks_di4_v6.0.2.sh
at1_v6.0.5.sh
at2_v6.0.2.sh
at3_v6.0.1.sh
gv1_v6.0.2.sh
gv2_v6.0.14.sh
gv3_v6.0.1.sh
gv4_v6.0.1.sh
gv5_v6.0.1.sh
unlocks_gv6_v6.0.2.sh
unlocks_gv7_v6.0.2.sh
"

RESULTS=""

for chk in $CHECKS; do
  script="tools/checks/$chk"
  if [ ! -x "$script" ]; then
    RESULTS="${RESULTS}${chk}: ⚠️ MISSING\n"
    continue
  fi

  OUTTXT="$(mktemp)"
  if "$script" >"$OUTTXT" 2>&1; then
    if grep -q "PASS" "$OUTTXT"; then
      RESULTS="${RESULTS}${chk}: ✅ PASS\n"
    else
      RESULTS="${RESULTS}${chk}: ⚠️ UNKNOWN\n"
    fi
  else
    if grep -q "FAIL" "$OUTTXT"; then
      RESULTS="${RESULTS}${chk}: ❌ FAIL\n"
    else
      RESULTS="${RESULTS}${chk}: ❌ FAIL\n"
    fi
  fi
  rm -f "$OUTTXT"
done

printf "%b" "$RESULTS" | tee "$OUT"
