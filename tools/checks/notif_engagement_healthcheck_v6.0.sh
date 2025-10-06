#!/bin/sh
set -eu

OUT="evidence/v6.0/notification_engagement/healthcheck.txt"
mkdir -p "$(dirname "$OUT")"

CHECKS="
notif_engagement_ee1_v6.0.4.sh
notif_engagement_ee2_v6.0.3.sh
notif_engagement_ee3_v6.0.7.sh
notif_engagement_ee4_v6.0.1.sh
notif_engagement_tc1_v6.0.15.sh
notif_engagement_tc2.sh
notif_engagement_tc3_v6.0.2.sh
notif_engagement_tc3_perms_v6.0.sh
notif_engagement_tc5_v6.0.1.sh
notif_engagement_tc6_v6.0.1.sh
notif_engagement_di1_v6.0.1.sh
notif_engagement_di2_v6.0.1.sh
notif_engagement_di3_v6.0.7.sh
notif_engagement_di4_v6.0.1.sh
notif_engagement_at1_v6.0.3.sh
notif_engagement_at2_v6.0.5.sh
notif_engagement_at3_v6.0.1.sh
notif_engagement_gv1_v6.0.1.sh
notif_engagement_gv2_v6.0.1.sh
notif_engagement_gv3_v6.0.1.sh
notif_engagement_gv4_v6.0.1.sh
notif_engagement_gv5_v6.0.1.sh
notif_engagement_gv6_v6.0.3.sh
notif_engagement_gv7_v6.0.15.sh
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
