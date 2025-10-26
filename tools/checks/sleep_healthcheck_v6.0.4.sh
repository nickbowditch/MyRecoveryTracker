#!/bin/sh
set -eu
OUT="evidence/v6.0/sleep/healthcheck.txt"
mkdir -p "$(dirname "$OUT")"

PKG="com.nick.myrecoverytracker"
APP_MAIN="$PKG/.MainActivity"

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
sleep_gv7_v6.0.4.sh
"

reset_adb() {
  adb get-state >/dev/null 2>&1 || { echo "❌ NO DEVICE"; exit 2; }
  adb shell am force-stop "$PKG" >/dev/null 2>&1 || true
  adb shell logcat -c >/dev/null 2>&1 || true
  adb shell am start -n "$APP_MAIN" >/dev/null 2>&1 || true
  sleep 0.5
}

run_check() {
  local chk="$1" OUTTXT
  OUTTXT="$(mktemp)"
  script="tools/checks/$chk"
  if [ ! -x "$script" ]; then
    printf "%s: ⚠️  MISSING\n" "$chk" | tee -a "$OUT"
    return
  fi
  reset_adb
  "$script" >"$OUTTXT" 2>&1 || true

  if grep -Eq '\bRESULT[:=]PASS\b' "$OUTTXT"; then
    printf "%s: ✅ PASS\n" "$chk" | tee -a "$OUT"
  elif grep -Eq '\bRESULT[:=]FAIL\b' "$OUTTXT"; then
    # if DI3 fails once, re-run once
    if [ "$chk" = "sleep_di3_v6.0.27.sh" ]; then
      sleep 1
      reset_adb
      "$script" >"$OUTTXT" 2>&1 || true
      if grep -Eq '\bRESULT[:=]PASS\b' "$OUTTXT"; then
        printf "%s: ✅ PASS (retry)\n" "$chk" | tee -a "$OUT"
      else
        printf "%s: ❌ FAIL\n" "$chk" | tee -a "$OUT"
      fi
    else
      printf "%s: ❌ FAIL\n" "$chk" | tee -a "$OUT"
    fi
  else
    printf "%s: ⚠️  UNKNOWN\n" "$chk" | tee -a "$OUT"
  fi
  rm -f "$OUTTXT"
  sleep 0.5
}

: > "$OUT"
for chk in $CHECKS; do
  run_check "$chk"
done
