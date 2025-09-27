#!/bin/sh
set -eu

PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/notification_engagement/ee3.1.txt"
RCV="$PKG/.TriggerReceiver"
ACT="$PKG.ACTION_RUN_ENGAGEMENT_ROLLUP"
NAME="EngagementRollup"

mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "EE-3 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG"  >/dev/null 2>&1 || { echo "EE-3 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

adb logcat -c >/dev/null 2>&1 || true
adb shell dumpsys deviceidle force-idle >/dev/null 2>&1 || true
adb shell cmd activity broadcast -n "$RCV" -a "$ACT" --receiver-foreground --user 0 >/dev/null 2>&1 || true
sleep 3

LOG="$(adb logcat -d 2>/dev/null | grep -iE 'TriggerReceiver|Engagement|Rollup|WorkManager' || true)"
WM_OK="$(adb shell dumpsys activity service WorkManager 2>/dev/null \
  | awk -v p="$PKG" 'BEGIN{IGNORECASE=1}
     ( $0 ~ p && $0 ~ /(Engagement|Rollup|Notif)/ && $0 ~ /(SUCCEEDED|SUCCESS|COMPLETED|FINISHED)/ ){print; exit}' || true)"

{
  echo "=== LOGCAT (idle->broadcast->completion) ==="
  [ -n "$LOG" ] && echo "$LOG" || echo "[none]"
  echo
  echo "=== WORKMANAGER STATUS ==="
  [ -n "$WM_OK" ] && echo "$WM_OK" || echo "[none]"
} | tee "$OUT" >/dev/null

if [ -n "$WM_OK" ] || printf '%s' "$LOG" | grep -qiE '(Engagement|Rollup).*(success|succeed|completed|finish|done)'; then
  echo "EE-3 RESULT=PASS" | tee -a "$OUT"; exit 0
else
  echo "EE-3 RESULT=FAIL" | tee -a "$OUT"; exit 1
fi
