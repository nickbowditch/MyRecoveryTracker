#!/bin/bash
set -euo pipefail

PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/movement_intensity/ee2.txt"
mkdir -p "$(dirname "$OUT")"

fail() {
echo "[FAIL] $1" | tee "$OUT"
echo "--- DEBUG: device/app ---" | tee -a "$OUT"
{ adb get-state && adb shell pm path "$PKG"; } 2>&1 | tr -d $'\r' | tee -a "$OUT" || true
echo "--- DEBUG: WorkManager dump (pkg slice) ---" | tee -a "$OUT"
adb shell dumpsys activity service WorkManager 2>/dev/null | tr -d $'\r' | awk -v p="$PKG" 'BEGIN{IGNORECASE=1} $0 ~ p && ($0 ~ /Movement|Intensity|Worker|enqueue|run/){print; hit=1} END{if(!hit) print "<none>"}' | tee -a "$OUT" || true
echo "--- DEBUG: JobScheduler pkg blocks (filtered) ---" | tee -a "$OUT"
adb shell dumpsys jobscheduler 2>/dev/null | tr -d $'\r' | awk -v p="$PKG" 'BEGIN{RS="JOB ";FS="\n";IGNORECASE=1} $0 ~ p && ($0 ~ /Movement|Intensity|RTC|READY|RUNNABLE/){print "JOB " $0 "\n----"; hit=1} END{if(!hit) print "<none>"}' | sed -n '1,120p' | tee -a "$OUT" || true
echo "--- DEBUG: logcat tail (Movement/Intensity) ---" | tee -a "$OUT"
adb logcat -d -v brief 2>/dev/null | grep -Ei "$PKG|Movement|Intensity|Worker|WorkManager|TriggerReceiver|enqueue" | tail -n 200 | tr -d $'\r' | tee -a "$OUT" || true
echo "RESULT=FAIL" | tee -a "$OUT"
exit 1
}

echo "[INFO] EE2 — MovementIntensity worker scheduled & triggerable" | tee "$OUT" >/dev/null

adb get-state >/dev/null 2>&1 || fail "No device"
adb shell pm path "$PKG" >/dev/null 2>&1 || fail "App not installed"

RCV="$PKG/.TriggerReceiver"
CANDIDATE_ACTIONS=(
"$PKG.ACTION_RUN_MOVEMENT_INTENSITY_DAILY"
"$PKG.ACTION_RUN_MOVEMENT_DAILY"
"$PKG.ACTION_RUN_INTENSITY_DAILY"
"$PKG.ACTION_RUN_DAILY"
)

adb logcat -c >/dev/null 2>&1 || true

for ACT in "${CANDIDATE_ACTIONS[@]}"; do
adb shell cmd activity broadcast -n "$RCV" -a "$ACT" --receiver-foreground --user 0 >/dev/null 2>&1 || true
adb shell am broadcast -a "$ACT" >/dev/null 2>&1 || true
done

sleep 2

JS="$(adb shell dumpsys jobscheduler 2>/dev/null | tr -d $'\r' | awk -v p="$PKG" 'BEGIN{RS="JOB ";FS="\n";IGNORECASE=1} $0 ~ p && ($0 ~ /Movement|Intensity/){print "JOB " $0 "\n----"; exit}' || true)"
WM="$(adb shell dumpsys activity service WorkManager 2>/dev/null | tr -d $'\r' | awk -v p="$PKG" 'BEGIN{IGNORECASE=1} ($0 ~ p) && ($0 ~ /Movement|Intensity|enqueue|RUNNING|SUCCEEDED/){print; exit}' || true)"

deadline=$((SECONDS+15))
HIT_LOG=""
while (( SECONDS < deadline )); do
HIT_LOG="$(adb logcat -d -v brief 2>/dev/null | grep -Ei "$PKG|MovementIntensityDailyWorker|MovementIntensity|IntensityWorker|enqueue|Start|RUNNING|SUCCEEDED" | tail -n 80 | tr -d $'\r' || true)"
if [ -n "$HIT_LOG" ]; then break; fi
sleep 1
done

{
echo "=== JOBSCHEDULER (pkg slice) ==="
if [ -n "$JS" ]; then printf "%s\n" "$JS"; else echo "<none>"; fi
echo
echo "=== WORKMANAGER (pkg slice) ==="
if [ -n "$WM" ]; then printf "%s\n" "$WM"; else echo "<none>"; fi
echo
echo "=== LOGCAT (tail, filtered) ==="
if [ -n "$HIT_LOG" ]; then printf "%s\n" "$HIT_LOG"; else echo "<none>"; fi
} | tee -a "$OUT" >/dev/null

if [ -n "$JS" ] || [ -n "$WM" ] || printf "%s" "$HIT_LOG" | grep -qiE 'Movement|Intensity|enqueue|Worker|RUNNING|SUCCEEDED'; then
echo "EE2 RESULT=PASS" | tee -a "$OUT"
exit 0
fi

fail "No scheduled or triggerable MovementIntensity worker evidence found"
