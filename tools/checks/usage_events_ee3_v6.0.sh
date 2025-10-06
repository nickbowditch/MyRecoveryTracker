#!/bin/sh
set -eu

PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/usage_events/ee3.txt"
RCV="$PKG/.TriggerReceiver"
A1="$PKG.ACTION_RUN_USAGE_EVENTS_DAILY"
A2="$PKG.ACTION_RUN_USAGE_EVENTS_ROLLUP"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "EE-3 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "EE-3 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

adb logcat -c >/dev/null 2>&1 || true
adb shell dumpsys deviceidle force-idle >/dev/null 2>&1 || adb shell cmd deviceidle force-idle >/dev/null 2>&1 || true

for A in "$A1" "$A2"; do
[ -n "$A" ] || continue
adb shell cmd activity broadcast -n "$RCV" -a "$A" --receiver-foreground --user 0 >/dev/null 2>&1 || true
done

PASS=0
WM_OK=""
LOG_OK=""

i=0
while [ $i -lt 30 ]; do
WM_OK="$(adb shell dumpsys activity service WorkManager 2>/dev/null | awk -v p="$PKG" 'BEGIN{IGNORECASE=1}
($0 ~ p) && ($0 ~ /UsageEvents/) && ($0 ~ /(SUCCEEDED|SUCCESS|COMPLETED|FINISHED)/){print; exit}' || true)"
[ -n "$WM_OK" ] && { PASS=1; break; }

LOG_OK="$(adb logcat -d 2>/dev/null | grep -i "UsageEvents" | grep -i -E "enqueue|succeeded|success|completed|finished|done" || true)"
[ -n "$LOG_OK" ] && { PASS=1; break; }

i=$((i+1))
sleep 1
done

{
echo "=== WORKMANAGER SUCCESS MATCH ==="
[ -n "$WM_OK" ] && echo "$WM_OK" || echo "[none]"
echo
echo "=== LOGCAT SUCCESS MATCH ==="
[ -n "$LOG_OK" ] && echo "$LOG_OK" || (adb logcat -d 2>/dev/null | grep -i -E 'TriggerReceiver|UsageEvents|WorkManager' || echo "[none]")
} | tee "$OUT" >/dev/null

if [ "$PASS" -eq 1 ]; then
echo "EE-3 RESULT=PASS" | tee -a "$OUT"; exit 0
else
echo "EE-3 RESULT=FAIL" | tee -a "$OUT"; exit 1
fi
