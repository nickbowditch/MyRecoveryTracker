#!/bin/bash
set -eu
PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/distance/ee2.txt"
mkdir -p "$(dirname "$OUT")"
exec > >(tee "$OUT") 2>&1

fail() {
  echo "[FAIL] $1"
  echo "RESULT=FAIL"
  exit 1
}

echo "[INFO] EE2 Workers scheduled & triggerable"
adb get-state >/dev/null 2>&1 || fail "No device"
adb shell pm path "$PKG" >/dev/null 2>&1 || fail "App not installed"

RCV="$PKG/.TriggerReceiver"
ACT="$PKG.ACTION_RUN_DISTANCE_DAILY"
adb logcat -c >/dev/null 2>&1 || true
adb shell cmd activity broadcast -n "$RCV" -a "$ACT" --receiver-foreground --user 0 >/dev/null 2>&1 || true
sleep 2

JS="$(adb shell dumpsys jobscheduler 2>/dev/null \
| awk -v p="$PKG" 'BEGIN{IGNORECASE=1; blk=""}
  /^JOB #/{blk=$0 ORS; next}
  {blk=blk $0 ORS}
  /READY|WAITING|RUNNABLE|RTC/{
    if (blk ~ p && blk ~ /Distance|Location|GPS/) {print blk; exit}
  }' || true)"

WM="$(adb shell dumpsys activity service WorkManager 2>/dev/null \
| awk -v p="$PKG" 'BEGIN{IGNORECASE=1}
  ($0 ~ p) && ($0 ~ /Distance|Location|GPS/){print; exit}' || true)"

LOG="$(adb logcat -d 2>/dev/null \
| grep -iE 'TriggerReceiver|WorkManager|enqueue|(Distance|Location|GPS)' || true)"

{
echo "=== JOBSCHEDULER ==="
[ -n "$JS" ] && echo "$JS" || echo "[none]"
echo
echo "=== WORKMANAGER ==="
[ -n "$WM" ] && echo "$WM" || echo "[none]"
echo
echo "=== LOGCAT ==="
[ -n "$LOG" ] && echo "$LOG" || echo "[none]"
} | tee "$OUT" >/dev/null

if [ -n "$JS" ] || [ -n "$WM" ] || printf '%s' "$LOG" | grep -qiE '(Distance|Location|GPS).(enqueue|worker|rollup|start|run)'; then
  echo "RESULT=PASS" | tee -a "$OUT"
  exit 0
else
  fail "No scheduled or triggerable worker evidence found"
fi
