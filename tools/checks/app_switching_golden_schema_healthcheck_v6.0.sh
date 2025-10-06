#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
RCV="$PKG/.TriggerReceiver"
CSV_SWITCH="files/daily_app_switching.csv"
CSV_EVENTS="files/usage_events.csv"
ACT_SWITCH="$PKG.ACTION_RUN_APP_SWITCHING_DAILY"
ACT_USAGE="$PKG.ACTION_RUN_USAGE_CAPTURE"
OUT="evidence/v6.0/app_switching/healthcheck.txt"
mkdir -p "$(dirname "$OUT")"

fail(){ echo "$1" | tee "$OUT"; exit 1; }

adb get-state >/dev/null 2>&1 || fail "HC RESULT=FAIL (no device)"
adb shell pm path "$PKG" >/dev/null 2>&1 || fail "HC RESULT=FAIL (app not installed)"

EVT_HDR_DEV="$(adb exec-out run-as "$PKG" sed -n '1p' "$CSV_EVENTS" 2>/dev/null | tr -d '\r' || true)"
SW_HDR_DEV_BEFORE="$(adb exec-out run-as "$PKG" sed -n '1p' "$CSV_SWITCH" 2>/dev/null | tr -d '\r' || true)"

SRC_WRITERS="$(grep -R --include='*.kt' --include='*.java' -nE 'File\(.*"daily_app_switching\.csv"' app/src/main/java 2>/dev/null || true)"
SRC_HDR_SWITCH_DECLS="$(grep -R --include='*.kt' --include='*.java' -nE 'ensureHeader\(.*"date,switches,entropy' app/src/main/java 2>/dev/null || true)"
SRC_HDR_PERPKG_DECLS="$(grep -R --include='*.kt' --include='*.java' -nE 'ensureHeader\(.*"date,package,starts' app/src/main/java 2>/dev/null || true)"

adb logcat -c >/dev/null 2>&1 || true

adb shell run-as "$PKG" sh <<IN
set -eu
rm -f "$CSV_SWITCH"
mkdir -p files
IN

adb shell cmd activity broadcast -n "$RCV" -a "$ACT_SWITCH" --receiver-foreground --user 0 >/dev/null 2>&1 || true
sleep 2
SW_HDR_AFTER_SWITCH="$(adb exec-out run-as "$PKG" sed -n '1p' "$CSV_SWITCH" 2>/dev/null | tr -d '\r' || true)"
SW_ROWS_AFTER_SWITCH="$(adb exec-out run-as "$PKG" head -n5 "$CSV_SWITCH" 2>/dev/null | tr -d '\r' || true)"

adb shell cmd activity broadcast -n "$RCV" -a "$ACT_USAGE" --receiver-foreground --user 0 >/dev/null 2>&1 || true
sleep 2
SW_HDR_AFTER_USAGE="$(adb exec-out run-as "$PKG" sed -n '1p' "$CSV_SWITCH" 2>/dev/null | tr -d '\r' || true)"
SW_ROWS_AFTER_USAGE="$(adb exec-out run-as "$PKG" head -n5 "$CSV_SWITCH" 2>/dev/null | tr -d '\r' || true)"

LOG_TAIL="$(adb logcat -d 2>/dev/null | grep -iE 'AppSwitchingDaily|AppSwitchingWorker|Switching|UsageCapture|TriggerReceiver' | tail -n 80 || true)"

{
  echo "=== GOLDEN SCHEMA EXPECTED ==="
  echo "files/usage_events.csv -> date,time,event_type,package"
  echo "files/daily_app_switching.csv -> date,switches,entropy"
  echo
  echo "=== DEVICE: current headers ==="
  echo "usage_events.csv: ${EVT_HDR_DEV:-[missing]}"
  echo "daily_app_switching.csv (before): ${SW_HDR_DEV_BEFORE:-[missing]}"
  echo
  echo "=== SOURCE: writers touching daily_app_switching.csv ==="
  [ -n "$SRC_WRITERS" ] && printf '%s\n' "$SRC_WRITERS" || echo "[none]"
  echo
  echo "=== SOURCE: schema decls (expected rollup header) ==="
  [ -n "$SRC_HDR_SWITCH_DECLS" ] && printf '%s\n' "$SRC_HDR_SWITCH_DECLS" || echo "[none]"
  echo
  echo "=== SOURCE: schema decls (per-package header) ==="
  [ -n "$SRC_HDR_PERPKG_DECLS" ] && printf '%s\n' "$SRC_HDR_PERPKG_DECLS" || echo "[none]"
  echo
  echo "=== RUNTIME: after ACTION_RUN_APP_SWITCHING_DAILY ==="
  echo "header: ${SW_HDR_AFTER_SWITCH:-[missing]}"
  echo "--- first 5 lines ---"
  [ -n "$SW_ROWS_AFTER_SWITCH" ] && printf '%s\n' "$SW_ROWS_AFTER_SWITCH" || echo "[none]"
  echo
  echo "=== RUNTIME: after ACTION_RUN_USAGE_CAPTURE ==="
  echo "header: ${SW_HDR_AFTER_USAGE:-[missing]}"
  echo "--- first 5 lines ---"
  [ -n "$SW_ROWS_AFTER_USAGE" ] && printf '%s\n' "$SW_ROWS_AFTER_USAGE" || echo "[none]"
  echo
  echo "=== LOGCAT (Switching/UsageCapture) ==="
  [ -n "$LOG_TAIL" ] && printf '%s\n' "$LOG_TAIL" || echo "[none]"
} | tee "$OUT" >/dev/null

RES="FAIL"
if [ "${EVT_HDR_DEV:-}" = "date,time,event_type,package" ] && [ "${SW_HDR_AFTER_SWITCH:-}" = "date,switches,entropy" ] && [ "${SW_HDR_AFTER_USAGE:-}" = "date,switches,entropy" ]; then
  RES="PASS"
fi

if [ "${SW_HDR_AFTER_SWITCH:-}" = "date,switches,entropy" ] && [ "${SW_HDR_AFTER_USAGE:-}" = "date,package,starts" ]; then
  echo "HC RESULT=FAIL (overwrite detected: UsageCapture switched daily_app_switching.csv to per-package schema)" | tee -a "$OUT"
  exit 1
fi

echo "HC RESULT=$RES" | tee -a "$OUT"
[ "$RES" = "PASS" ] && exit 0 || exit 1
