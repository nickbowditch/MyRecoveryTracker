#!/bin/sh
set -eu

PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/usage_events/ee2.txt"
RCV="$PKG/.TriggerReceiver"
ACT1="$PKG.ACTION_RUN_USAGE_EVENTS_DAILY"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "EE-2 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG"  >/dev/null 2>&1 || { echo "EE-2 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

adb logcat -c >/dev/null 2>&1 || true
adb shell cmd activity broadcast -n "$RCV" -a "$ACT1" --receiver-foreground --user 0 >/dev/null 2>&1 || true
sleep 2

JS="$(adb shell dumpsys jobscheduler 2>/dev/null \
| awk -v p="$PKG" 'BEGIN{IGNORECASE=1; keep=0}
/JOB #/{blk=$0 ORS; keep=0; next}
/u[0-9]+a[0-9]+/ {blk=blk $0 ORS}
/JobInfo/ {blk=blk $0 ORS}
{blk=blk $0 ORS}
/RTC|READY|WAITING/{
if (blk ~ p && blk ~ /UsageEvents/) {print blk; exit}
}' || true)"

WM="$(adb shell dumpsys activity service WorkManager 2>/dev/null \
| awk -v p="$PKG" 'BEGIN{IGNORECASE=1}
{line=$0}
(line ~ p) && (line ~ /UsageEvents/){print line; found=1; exit}
END{if(!found) exit 0}' || true)"

LOG="$(adb logcat -d 2>/dev/null \
| grep -iE 'TriggerReceiver|WorkManager|enqueue|UsageEvents' || true)"

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

if [ -n "$JS" ] || [ -n "$WM" ] || printf '%s' "$LOG" | grep -qiE 'UsageEvents.*(enqueue|worker|rollup)'; then
echo "EE-2 RESULT=PASS" | tee -a "$OUT"; exit 0
else
echo "EE-2 RESULT=FAIL" | tee -a "$OUT"; exit 1
fi
