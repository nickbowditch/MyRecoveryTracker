#!/bin/sh
set -eu

OUT="evidence/v6.0/notification_latency/healthcheck.txt"
mkdir -p "$(dirname "$OUT")"

CHECKS="
notif_latency_ee1_v6.0.sh
notif_latency_ee2_v6.0.sh
notif_latency_ee3_v6.0.sh
notif_latency_ee4_v6.0.1.sh
notif_latency_tc1_v6.0.16.sh
notif_latency_tc2_v6.0.21.sh
notif_latency_tc3_v6.0.1.sh
notif_latency_tc5_v6.0.1.sh
notif_latency_tc6_v6.0.1.sh
notif_latency_di1_v6.0.1.sh
notif_latency_di2_v6.0.2.sh
notif_latency_di3_v6.0.1.sh
notif_latency_di4_v6.0.1.sh
notif_latency_at1_v6.0.3.sh
notif_latency_at2_v6.1.4.sh
notif_latency_at3_v6.0.1.sh
notif_latency_gv1_v6.0.1.sh
notif_latency_gv2_v6.0.1.sh
notif_latency_gv3_v6.0.1.sh
notif_latency_gv4_v6.0.1.sh
notif_latency_gv5_v6.0.1.sh
notif_latency_gv6_v6.0.3.sh
notif_latency_gv7_v6.0.2.sh
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
