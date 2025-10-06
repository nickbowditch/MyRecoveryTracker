#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/app_switching/diag_schema_conflict.txt"
mkdir -p "$(dirname "$OUT")"

exp_hdr="date,switches,entropy"
perpkg_hdr="date,package,starts"
csv="files/daily_app_switching.csv"
RCV="$PKG/.TriggerReceiver"
ACT_SWITCH="$PKG.ACTION_RUN_APP_SWITCHING_DAILY"
ACT_USAGE="$PKG.ACTION_RUN_USAGE_CAPTURE"

adb get-state >/dev/null 2>&1 || { echo "DIAG RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "DIAG RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

SRC_FILES="$(git ls-files 'app/src/main/java/**/*.kt' 'app/src/main/java/**/*.java' 2>/dev/null || true)"

SRC_WRITE_SWITCH="$(grep -R --include='*.kt' --include='*.java' -nE 'File\(.*"daily_app_switching\.csv"' app/src/main/java 2>/dev/null || true)"
SRC_HDR_SWITCH="$(grep -R --include='*.kt' --include='*.java' -nE 'ensureHeader\(.*"date,switches,entropy"|writeText\("date,switches,entropy' app/src/main/java 2>/dev/null || true)"
SRC_HDR_PERPKG="$(grep -R --include='*.kt' --include='*.java' -nE 'writeText\("date,package,starts' app/src/main/java 2>/dev/null || true)"
SRC_ANY_PERPKG_REF="$(grep -R --include='*.kt' --include='*.java' -nE '"date,package,starts"|package,starts' app/src/main/java 2>/dev/null || true)"

HDR_BEFORE="$(adb exec-out run-as "$PKG" sed -n '1p' "$csv" 2>/dev/null | tr -d '\r' || true)"

adb logcat -c >/dev/null 2>&1 || true
adb shell run-as "$PKG" sh <<INP >/dev/null 2>&1 || true
set -eu
rm -f "$csv"
INP
adb shell cmd activity broadcast -n "$RCV" -a "$ACT_SWITCH" --receiver-foreground --user 0 >/dev/null 2>&1 || true
sleep 2

HDR_AFTER_SWITCH="$(adb exec-out run-as "$PKG" sed -n '1p' "$csv" 2>/dev/null | tr -d '\r' || true)"
ROWS_SWITCH="$(adb exec-out run-as "$PKG" head -n5 "$csv" 2>/dev/null | tr -d '\r' || true)"

adb shell cmd activity broadcast -n "$RCV" -a "$ACT_USAGE" --receiver-foreground --user 0 >/dev/null 2>&1 || true
sleep 2

HDR_AFTER_USAGE="$(adb exec-out run-as "$PKG" sed -n '1p' "$csv" 2>/dev/null | tr -d '\r' || true)"
ROWS_USAGE="$(adb exec-out run-as "$PKG" head -n5 "$csv" 2>/dev/null | tr -d '\r' || true)"

LOG_SWITCH="$(adb logcat -d 2>/dev/null | grep -iE 'AppSwitchingDaily|AppSwitchingWorker|Switching|TriggerReceiver' || true)"

{
  echo "=== SOURCE: writers referencing daily_app_switching.csv ==="
  [ -n "$SRC_WRITE_SWITCH" ] && printf '%s\n' "$SRC_WRITE_SWITCH" || echo "[none]"
  echo
  echo "=== SOURCE: schema declarations (expected) ==="
  [ -n "$SRC_HDR_SWITCH" ] && printf '%s\n' "$SRC_HDR_SWITCH" || echo "[none]"
  echo
  echo "=== SOURCE: schema declarations (per-package) ==="
  [ -n "$SRC_HDR_PERPKG" ] && printf '%s\n' "$SRC_HDR_PERPKG" || echo "[none]"
  [ -n "$SRC_ANY_PERPKG_REF" ] && printf '%s\n' "$SRC_ANY_PERPKG_REF" || :
  echo
  echo "=== DEVICE: header before ==="
  echo "${HDR_BEFORE:-[missing]}"
  echo
  echo "=== Trigger: ACTION_RUN_APP_SWITCHING_DAILY -> header after ==="
  echo "${HDR_AFTER_SWITCH:-[missing]}"
  echo "--- first 5 lines ---"
  [ -n "$ROWS_SWITCH" ] && printf '%s\n' "$ROWS_SWITCH" || echo "[none]"
  echo
  echo "=== Trigger: ACTION_RUN_USAGE_CAPTURE -> header after ==="
  echo "${HDR_AFTER_USAGE:-[missing]}"
  echo "--- first 5 lines ---"
  [ -n "$ROWS_USAGE" ] && printf '%s\n' "$ROWS_USAGE" || echo "[none]"
  echo
  echo "=== LOGCAT (Switch) ==="
  [ -n "$LOG_SWITCH" ] && printf '%s\n' "$LOG_SWITCH" || echo "[none]"
} | tee "$OUT" >/dev/null

conflict=1
[ "${HDR_AFTER_SWITCH:-}" = "$exp_hdr" ] && conflict=0
[ "${HDR_AFTER_USAGE:-}" = "$perpkg_hdr" ] && conflict=2

case "$conflict" in
  0) echo "DIAG RESULT=PASS (SwitchingDaily writes expected schema: $exp_hdr)" | tee -a "$OUT"; exit 0 ;;
  2) echo "DIAG RESULT=FAIL (UsageCapture overwrites $csv with per-package schema: $perpkg_hdr)" | tee -a "$OUT"; exit 1 ;;
  *) 
     if [ -n "${HDR_AFTER_SWITCH:-}" ] && [ "${HDR_AFTER_SWITCH:-}" != "$exp_hdr" ]; then
       echo "DIAG RESULT=FAIL (schema drift after SWITCH trigger: '${HDR_AFTER_SWITCH:-}')" | tee -a "$OUT"; exit 1
     fi
     echo "DIAG RESULT=FAIL (no clear writer or unexpected state)" | tee -a "$OUT"; exit 1
  ;;
esac
