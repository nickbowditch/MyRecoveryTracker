#!/bin/sh
set -eu

PKG="com.nick.myrecoverytracker"
RCV="$PKG/.TriggerReceiver"
ACT="$PKG.ACTION_RUN_APP_SWITCHING_DAILY"
CSV="files/daily_app_switching.csv"
OUT="evidence/v6.0/app_switching/ee2.txt"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "EE-2 RESULT=FAIL (no device/emulator)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "EE-2 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

adb shell run-as "$PKG" rm -f "$CSV" >/dev/null 2>&1 || true
adb logcat -c >/dev/null 2>&1 || true

adb shell cmd activity broadcast -n "$RCV" -a "$ACT" --receiver-foreground --user 0 >/dev/null 2>&1 || true

deadline=$(( $(date +%s) + 30 ))
HDR=""
while :; do
  HDR="$(adb exec-out run-as "$PKG" sed -n '1p' "$CSV" 2>/dev/null | tr -d '\r' || true)"
  [ -n "$HDR" ] && break
  [ "$(date +%s)" -ge "$deadline" ] && break
  sleep 1
done

T="$(adb shell toybox date +%F | tr -d '\r')"
Y="$(adb shell toybox date -d "@$(( $(adb shell toybox date +%s)-86400 ))" +%F | tr -d '\r')"

ROW_T="$(adb exec-out run-as "$PKG" awk -F, -v d="$T" 'NR>1&&$1==d{print;exit}' "$CSV" 2>/dev/null | tr -d '\r' || true)"
ROW_Y="$(adb exec-out run-as "$PKG" awk -F, -v d="$Y" 'NR>1&&$1==d{print;exit}' "$CSV" 2>/dev/null | tr -d '\r' || true)"

LOGS="$(adb logcat -d 2>/dev/null | grep -iE 'TriggerReceiver|AppSwitchingDaily|WM-WorkerWrapper|WorkManager|enqueue|succeed|success|completed|finished' | tail -n 200 || true)"

{
  echo "=== HEADER ==="
  echo "${HDR:-[missing]}"
  echo
  echo "=== TODAY ($T) ==="
  [ -n "$ROW_T" ] && echo "$ROW_T" || echo "[none]"
  echo
  echo "=== YESTERDAY ($Y) ==="
  [ -n "$ROW_Y" ] && echo "$ROW_Y" || echo "[none]"
  echo
  echo "=== LOGCAT ==="
  [ -n "$LOGS" ] && echo "$LOGS" || echo "[none]"
} | tee "$OUT" >/dev/null

ok_hdr=0
[ "$HDR" = "date,switches,entropy" ] && ok_hdr=1

ok_logs=0
printf '%s\n' "$LOGS" | grep -qi 'Enqueue AppSwitchingDailyWorker' && ok_logs=1
if ! printf '%s\n' "$LOGS" | grep -qi 'Enqueue AppSwitchingDailyWorker'; then
  printf '%s\n' "$LOGS" | grep -qi 'TriggerReceiver: onReceive action=.*ACTION_RUN_APP_SWITCHING_DAILY' && ok_logs=1
fi
printf '%s\n' "$LOGS" | grep -qiE 'WM-WorkerWrapper.*Worker result SUCCESS.*AppSwitchingDailyWorker|AppSwitchingDailyWorker: AppSwitchingDaily' || ok_logs=0

if [ "$ok_hdr" -eq 1 ] && [ "$ok_logs" -eq 1 ]; then
  echo "EE-2 RESULT=PASS" | tee -a "$OUT"
  exit 0
fi

echo "EE-2 RESULT=FAIL" | tee -a "$OUT"
exit 1
