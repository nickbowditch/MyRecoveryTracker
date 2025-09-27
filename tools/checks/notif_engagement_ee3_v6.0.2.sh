#!/bin/sh
set -eu

PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/notification_engagement/ee3.2.txt"
RCV="$PKG/.TriggerReceiver"
ACT="$PKG.ACTION_RUN_ENGAGEMENT_ROLLUP"
NAME="EngagementRollup"

mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "EE-3 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG"  >/dev/null 2>&1 || { echo "EE-3 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

adb logcat -c >/dev/null 2>&1 || true
adb shell dumpsys deviceidle force-idle >/dev/null 2>&1 || true
adb shell cmd activity broadcast -n "$RCV" -a "$ACT" --receiver-foreground --user 0 >/dev/null 2>&1 || true

PASS=0
for i in $(seq 1 20); do
  WM_OK="$(adb shell dumpsys activity service WorkManager 2>/dev/null \
    | awk -v p="$PKG" 'BEGIN{IGNORECASE=1}
       ($0 ~ p && $0 ~ /(EngagementRollup|Engagement|Notif)/ && $0 ~ /(SUCCEEDED|SUCCESS|COMPLETED|FINISHED)/){print; exit}' || true)"
  LOG_OK="$(adb logcat -d 2>/dev/null | grep -iE '(EngagementRollup|Engagement).*(SUCCEEDED|SUCCESS|COMPLETED|FINISHED|DONE)' || true)"
  if [ -n "$WM_OK" ] || [ -n "$LOG_OK" ]; then PASS=1; break; fi
  sleep 1
done

{
  echo "=== WORKMANAGER STATUS ==="
  [ -n "${WM_OK:-}" ] && echo "$WM_OK" || echo "[none]"
  echo
  echo "=== LOGCAT (idle->broadcast->completion) ==="
  [ -n "${LOG_OK:-}" ] && echo "$LOG_OK" || adb logcat -d | grep -iE 'TriggerReceiver|Engagement|Rollup|WorkManager' || echo "[none]"
} | tee "$OUT" >/dev/null

if [ "$PASS" -eq 1 ]; then
  echo "EE-3 RESULT=PASS" | tee -a "$OUT"; exit 0
else
  echo "EE-3 RESULT=FAIL" | tee -a "$OUT"; exit 1
fi
