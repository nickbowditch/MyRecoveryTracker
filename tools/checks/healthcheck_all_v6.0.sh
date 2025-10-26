#!/bin/sh
set -eu

PKG="com.nick.myrecoverytracker"
APP_MAIN="$PKG/.MainActivity"
OUTDIR="evidence/v6.0/ALL_HEALTHCHECK"
SUMMARY="$OUTDIR/summary.txt"
mkdir -p "$OUTDIR"
: > "$SUMMARY"

adb get-state >/dev/null 2>&1 || { echo "❌ NO DEVICE"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "❌ APP NOT INSTALLED"; exit 3; }

latest_check() {
  # takes a glob like tools/checks/sleep_healthcheck_v6.0*.sh
  pat="$1"
  # shellcheck disable=SC2086
  ls -1 $pat 2>/dev/null | sort -V | tail -n1 || true
}

reset_env() {
  adb shell am force-stop "$PKG" >/dev/null 2>&1 || true
  adb shell logcat -c >/dev/null 2>&1 || true
  adb shell svc power stayon usb >/dev/null 2>&1 || true
  adb shell am start -n "$APP_MAIN" >/dev/null 2>&1 || true
  sleep 1
}

summarize_fail_causes() {
  tmpfile="$1"; outprefix="$2"
  fail_list="$OUTDIR/${outprefix}.fail.list.txt"
  grep -E 'FAIL|❌|RESULT[[:space:]]*[:=][[:space:]]*FAIL' "$tmpfile" > "$fail_list" || true
  culprits="$(grep -Eo '([A-Za-z0-9_./-]+\.sh)' "$fail_list" | sort -u | tr '\n' ' ' || true)"
  printf "%s" "$culprits" | sed 's/[[:space:]]*$//' > "$OUTDIR/${outprefix}.culprits.txt"
  echo "$culprits"
}

run_one() {
  s="$1"
  base="$(basename "$s")"
  [ -n "$s" ] && [ -x "$s" ] || { printf "%s: ⚠️ MISSING\n" "$base" | tee -a "$SUMMARY"; return; }
  reset_env
  TMP="$(mktemp)"
  if "$s" >"$TMP" 2>&1; then
    pass="$(grep -Eo '[A-Z0-9_-]+[[:space:]]+RESULT[[:space:]][:=][[:space:]]PASS([[:space:]]|$)' "$TMP" | tail -n1 || true)"
    [ -n "$pass" ] || pass="$base: PASS ✅"
    printf "%s\n" "$pass" | tee -a "$SUMMARY"
  else
    culprits="$(summarize_fail_causes "$TMP" "$base")"
    fail="$(grep -Eo '[A-Z0-9_-]+[[:space:]]+RESULT[[:space:]][:=][[:space:]]FAIL([^$]*)' "$TMP" | tail -n1 || true)"
    [ -n "$fail" ] || fail="$base: FAIL ❌"
    if [ -n "$culprits" ]; then
      printf "%s\n" "$fail (likely: $culprits) ❌" | tee -a "$SUMMARY"
    else
      printf "%s\n" "$fail ❌" | tee -a "$SUMMARY"
    fi
    dbg="$OUTDIR/${base}.debug.txt"
    {
      echo "----- DEBUG ${base} -----"
      tail -n 400 "$TMP" || true
      echo "----- END DEBUG ${base} -----"
    } > "$dbg"
  fi
  rm -f "$TMP"
}

CHECKS="
$(latest_check tools/checks/unlocks_healthcheck_v6.0*.sh)
$(latest_check tools/checks/sleep_healthcheck_v6.0*.sh)
$(latest_check tools/checks/lnsu_healthcheck_v6.0*.sh)
$(latest_check tools/checks/notif_engagement_healthcheck_v6.0*.sh)
$(latest_check tools/checks/notif_latency_healthcheck_v6.0*.sh)
$(latest_check tools/checks/usage_events_healthcheck_v6.0*.sh)
$(latest_check tools/checks/app_usage_by_category_healthcheck_v6.0*.sh)
$(latest_check tools/checks/app_switching_healthcheck_v6.0*.sh)
$(latest_check tools/checks/distance_healthcheck_v6.0*.sh)
$(latest_check tools/checks/movement_intensity_healthcheck_v6.0*.sh)
"

START_TS="$(date +%F_%H-%M-%S)"
echo "START $START_TS" | tee -a "$SUMMARY"

N_TOTAL=0
for S in $CHECKS; do
  [ -n "$S" ] || continue
  N_TOTAL=$((N_TOTAL+1))
  run_one "$S"
done

END_TS="$(date +%F_%H-%M-%S)"
echo "END $END_TS" | tee -a "$SUMMARY"
echo "TOTAL=$N_TOTAL" | tee -a "$SUMMARY"
