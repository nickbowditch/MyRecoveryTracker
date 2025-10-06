#!/bin/sh
set -eu

PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/app_switching/ee3.txt"
RCV="$PKG/.TriggerReceiver"
ACT="$PKG.ACTION_RUN_APP_SWITCHING_DAILY"
CSV="files/daily_app_switching.csv"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "EE-3 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "EE-3 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

adb logcat -c >/dev/null 2>&1 || true
adb shell dumpsys deviceidle force-idle >/dev/null 2>&1 || adb shell cmd deviceidle force-idle >/dev/null 2>&1 || true

adb shell run-as "$PKG" rm -f "$CSV" >/dev/null 2>&1 || true

adb shell cmd activity broadcast -n "$RCV" -a "$ACT" --receiver-foreground --user 0 >/dev/null 2>&1 || true

PASS=0
WM_OK=""
LOG_OK=""
HDR=""
i=0

while [ $i -lt 30 ]; do
HDR="$(adb exec-out run-as "$PKG" sed -n '1p' "$CSV" 2>/dev/null | tr -d '\r' || true)"
[ "$HDR" = "date,switches,entropy" ] && { PASS=1; break; }

WM_OK="$(adb shell dumpsys activity service WorkManager 2>/dev/null | awk -v p="$PKG" 'BEGIN{IGNORECASE=1}
($0 ~ p) && ($0 ~ /(AppSwitchingDaily|AppSwitching)/) && ($0 ~ /(SUCCEEDED|SUCCESS|COMPLETED|FINISHED)/){print; exit}' || true)"
[ -n "$WM_OK" ] && { PASS=1; break; }

LOG_OK="$(adb logcat -d 2>/dev/null | grep -iE '(AppSwitchingDaily|AppSwitchingWorker)' | grep -iE 'enqueue|succeeded|success|completed|finished|done' || true)"
[ -n "$LOG_OK" ] && { PASS=1; break; }

i=$((i+1))
sleep 1
done

{
echo "=== HEADER ==="
echo "${HDR:-[missing]}"
echo
echo "=== WORKMANAGER SUCCESS MATCH ==="
[ -n "$WM_OK" ] && echo "$WM_OK" || echo "[none]"
echo
echo "=== LOGCAT SUCCESS MATCH ==="
[ -n "$LOG_OK" ] && echo "$LOG_OK" || (adb logcat -d 2>/dev/null | grep -iE 'TriggerReceiver|AppSwitchingDaily|WorkManager' || echo "[none]")
} | tee "$OUT" >/dev/null

if [ "$PASS" -eq 1 ]; then
echo "EE-3 RESULT=PASS" | tee -a "$OUT"; exit 0
else
echo "EE-3 RESULT=FAIL" | tee -a "$OUT"; exit 1
fi
