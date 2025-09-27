#!/bin/sh
set -eu

PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/notification_engagement/ee2.1.txt"
NAME="EngagementRollup"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "EE-2 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "EE-2 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

adb logcat -c >/dev/null 2>&1 || true
sleep 1

JS="$(adb shell dumpsys jobscheduler 2>/dev/null | grep -i "$PKG" -A4 | grep -i "$NAME" -m1 || true)"
WM="$(adb shell dumpsys activity service WorkManager 2>/dev/null | grep -i "$PKG" -A8 | grep -i "$NAME" -m1 || true)"
LOG="$(adb logcat -d 2>/dev/null | grep -i -E 'TriggerReceiver|Boot|Package|EngagementRollup' || true)"

{
  echo "=== JOBSCHEDULER MATCH ==="
  [ -n "$JS" ] && echo "$JS" || echo "[none]"
  echo
  echo "=== WORKMANAGER MATCH ==="
  [ -n "$WM" ] && echo "$WM" || echo "[none]"
  echo
  echo "=== LOGCAT (Trigger/Boot/Update/EngagementRollup) ==="
  [ -n "$LOG" ] && echo "$LOG" || echo "[none]"
} | tee "$OUT" >/dev/null

if [ -n "$JS" ] || [ -n "$WM" ] || printf '%s' "$LOG" | grep -qi "$NAME"; then
  echo "EE-2 RESULT=PASS" | tee -a "$OUT"
  exit 0
else
  echo "EE-2 RESULT=FAIL" | tee -a "$OUT"
  exit 1
fi
