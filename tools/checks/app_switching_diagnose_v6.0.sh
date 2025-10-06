#!/bin/sh
set -eu

PKG="com.nick.myrecoverytracker"
CSV="files/daily_app_switching.csv"
OUT="evidence/v6.0/app_switching/diagnose.txt"
RCV="$PKG/.TriggerReceiver"
ACT_SWITCH="$PKG.ACTION_RUN_APP_SWITCHING_DAILY"
ACT_USAGE="$PKG.ACTION_RUN_USAGE_CAPTURE"
mkdir -p "$(dirname "$OUT")"

fail(){ echo "DIAG RESULT=FAIL $1" | tee "$OUT"; exit 1; }

adb get-state >/dev/null 2>&1 || fail "(no device)"
adb shell pm path "$PKG" >/dev/null 2>&1 || fail "(app not installed)"

SRC_WRITERS="$(grep -R --include='*.kt' --include='*.java' -nE 'File\(.*"daily_app_switching\.csv"' app/src/main/java 2>/dev/null || true)"
SRC_HDR_SWITCH="$(grep -R --include='*.kt' --include='*.java' -nE 'ensureHeader\(.*"date,switches,entropy' app/src/main/java 2>/dev/null || true)"
SRC_HDR_PERPKG="$(grep -R --include='*.kt' --include='*.java' -nE 'ensureHeader\(.*"date,package,starts' app/src/main/java 2>/dev/null || true)"

HDR_BEFORE="$(adb exec-out run-as "$PKG" sed -n '1p' "$CSV" 2>/dev/null | tr -d '\r' || true)"

adb logcat -c >/dev/null 2>&1 || true
adb shell run-as "$PKG" rm -f "$CSV" >/dev/null 2>&1 || true

adb shell cmd activity broadcast -n "$RCV" -a "$ACT_SWITCH" --receiver-foreground --user 0 >/dev/null 2>&1 || true
sleep 2
HDR_AFTER_SWITCH="$(adb exec-out run-as "$PKG" sed -n '1p' "$CSV" 2>/dev/null | tr -d '\r' || true)"
ROWS_AFTER_SWITCH="$(adb exec-out run-as "$PKG" head -n5 "$CSV" 2>/dev/null | tr -d '\r' || true)"

adb shell cmd activity broadcast -n "$RCV" -a "$ACT_USAGE" --receiver-foreground --user 0 >/dev/null 2>&1 || true
sleep 2
HDR_AFTER_USAGE="$(adb exec-out run-as "$PKG" sed -n '1p' "$CSV" 2>/dev/null | tr -d '\r' || true)"
ROWS_AFTER_USAGE="$(adb exec-out run-as "$PKG" head -n5 "$CSV" 2>/dev/null | tr -d '\r' || true)"

LOG_TAIL="$(adb logcat -d 2>/dev/null | grep -iE 'AppSwitchingDaily|AppSwitchingWorker|Switching|UsageCapture|TriggerReceiver' | tail -n 80 || true)"

{
  echo "=== SOURCE: writers referencing daily_app_switching.csv ==="
  [ -n "$SRC_WRITERS" ] && printf '%s\n' "$SRC_WRITERS" || echo "[none]"

  echo
  echo "=== SOURCE: schema declarations (rollup expected) ==="
  [ -n "$SRC_HDR_SWITCH" ] && printf '%s\n' "$SRC_HDR_SWITCH" || echo "[none]"

  echo
  echo "=== SOURCE: schema declarations (per-package) ==="
  [ -n "$SRC_HDR_PERPKG" ] && printf '%s\n' "$SRC_HDR_PERPKG" || echo "[none]"

  echo
  echo "=== DEVICE: header before ==="
  echo "${HDR_BEFORE:-[missing]}"

  echo
  echo "=== After ACTION_RUN_APP_SWITCHING_DAILY ==="
  echo "header=${HDR_AFTER_SWITCH:-[missing]}"
  echo "--- first 5 lines ---"
  [ -n "$ROWS_AFTER_SWITCH" ] && printf '%s\n' "$ROWS_AFTER_SWITCH" || echo "[none]"

  echo
  echo "=== After ACTION_RUN_USAGE_CAPTURE ==="
  echo "header=${HDR_AFTER_USAGE:-[missing]}"
  echo "--- first 5 lines ---"
  [ -n "$ROWS_AFTER_USAGE" ] && printf '%s\n' "$ROWS_AFTER_USAGE" || echo "[none]"

  echo
  echo "=== LOGCAT (Switching/UsageCapture) ==="
  [ -n "$LOG_TAIL" ] && printf '%s\n' "$LOG_TAIL" || echo "[none]"
} | tee "$OUT" >/dev/null

if [ "${HDR_AFTER_SWITCH:-}" = "date,switches,entropy" ] && [ "${HDR_AFTER_USAGE:-}" = "date,package,starts" ]; then
  echo "DIAG RESULT=FAIL (Same file used by two schemas; UsageCapture overwrites daily rollup)" | tee -a "$OUT"
  exit 1
fi

if [ -z "${HDR_AFTER_SWITCH:-}" ]; then
  echo "DIAG RESULT=FAIL (SwitchingDaily did not produce $CSV)" | tee -a "$OUT"
  exit 1
fi

if [ "${HDR_AFTER_SWITCH:-}" = "date,switches,entropy" ] && [ "${HDR_AFTER_USAGE:-}" = "date,switches,entropy" ]; then
  echo "DIAG RESULT=PASS (Single schema present: date,switches,entropy)" | tee -a "$OUT"
  exit 0
fi

echo "DIAG RESULT=FAIL (Unexpected headers; investigate writers)" | tee -a "$OUT"
exit 1
