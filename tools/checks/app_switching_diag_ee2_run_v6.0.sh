#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
RCV="$PKG/.TriggerReceiver"
ACT="$PKG.ACTION_RUN_APP_SWITCHING_DAILY"
OUT="evidence/v6.0/app_switching/ee2.run_diag.txt"
CSV="files/daily_app_switching.csv"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "EE2 DIAG=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "EE2 DIAG=FAIL (app not installed)" | tee "$OUT"; exit 3; }

adb shell run-as "$PKG" rm -f "$CSV" >/dev/null 2>&1 || true
adb logcat -c >/dev/null 2>&1 || true

adb shell cmd activity broadcast -n "$RCV" -a "$ACT" --receiver-foreground --user 0 >/dev/null 2>&1 || true

deadline=$(( $(date +%s) + 25 ))
HDR=""
while :; do
  HDR="$(adb exec-out run-as "$PKG" sed -n '1p' "$CSV" 2>/dev/null | tr -d '\r' || true)"
  [ -n "$HDR" ] && break
  [ "$(date +%s)" -ge "$deadline" ] && break
  sleep 1
done

WM="$(adb shell dumpsys activity service WorkManager 2>/dev/null | grep -iE 'AppSwitchingDaily|com\.nick\.myrecoverytracker\.AppSwitchingDailyWorker' -A2 -B2 || true)"
JS="$(adb shell dumpsys jobscheduler 2>/dev/null | sed -n '/^JOB /,/^$/p' | grep -iE 'AppSwitch|Switching|com\.nick\.myrecoverytracker' -A2 -B2 || true)"
LOG="$(adb logcat -d 2>/dev/null | grep -iE 'TriggerReceiver|AppSwitchingDaily|WorkManager|enqueue|SUCC|SUCCESS|COMPLET|FINISH' || true)"

{
  echo "ACTION=$ACT"
  echo "HEADER=${HDR:-[missing]}"
  echo
  echo "=== WM ==="
  [ -n "$WM" ] && echo "$WM" || echo "[none]"
  echo
  echo "=== JS ==="
  [ -n "$JS" ] && echo "$JS" || echo "[none]"
  echo
  echo "=== LOG ==="
  [ -n "$LOG" ] && echo "$LOG" || echo "[none]"
} | tee "$OUT" >/dev/null

[ "$HDR" = "date,switches,entropy" ] && { echo "EE2 DIAG=PASS" | tee -a "$OUT"; exit 0; }
echo "EE2 DIAG=FAIL" | tee -a "$OUT"; exit 1
