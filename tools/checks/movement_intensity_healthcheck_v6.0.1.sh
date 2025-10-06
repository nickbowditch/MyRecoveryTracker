#!/bin/sh
set -eu
OUT="evidence/v6.0/movement_intensity/healthcheck.txt"
mkdir -p "$(dirname "$OUT")"

CHECKS="
movement_intensity_ee1_v6.0.5.sh
movement_intensity_ee2_v6.0.sh
movement_intensity_ee3.sh
movement_intensity_ee4.sh
movement_intensity_tc1.sh
movement_intensity_tc2.sh
movement_intensity_tc3.sh
movement_intensity_tc5.sh
movement_intensity_tc6.sh
movement_intensity_di1.sh
movement_intensity_di2.sh
movement_intensity_di3.sh
movement_intensity_di4.sh
movement_intensity_at1.sh
movement_intensity_at2.sh
movement_intensity_at3_v6.0.2.sh
movement_intensity_gv1_v6.0.1.sh
movement_intensity_gv2_v6.0.3.sh
movement_intensity_gv3_v6.0.1.sh
movement_intensity_gv4_v6.0.1.sh
movement_intensity_gv5_v6.0.2.sh
movement_intensity_gv6_v6.0.1.sh
movement_intensity_gv7_v6.0.1.sh
"

PASS_RE='[A-Z0-9-]+[[:space:]]+RESULT[[:space:]][:=][[:space:]]PASS(\b|$)'
FAIL_RE='[A-Z0-9-]+[[:space:]]+RESULT[[:space:]][:=][[:space:]]FAIL(\b|$)'

RESULTS=""
: > "$OUT"

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
    {
      echo
      echo "----- DEBUG $chk -----"
      tail -n 200 "$OUTTXT" || true
      echo "----- END DEBUG $chk -----"
    } >> "$OUT"
  fi
  RESULTS="${RESULTS}${msg}\n"
  rm -f "$OUTTXT"
done

printf "%b" "$RESULTS" | tee -a "$OUT"
