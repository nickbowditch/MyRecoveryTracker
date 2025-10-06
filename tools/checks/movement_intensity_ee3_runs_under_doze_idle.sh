#!/bin/bash
set -euo pipefail

PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/movement_intensity/ee3.txt"
CSV="files/daily_movement_intensity.csv"
RCV="$PKG/.TriggerReceiver"
ACTIONS=(
  "$PKG.ACTION_RUN_MOVEMENT_INTENSITY_DAILY"
  "$PKG.ACTION_RUN_INTENSITY_DAILY"
  "$PKG.ACTION_RUN_MOVEMENT_DAILY"
  "$PKG.ACTION_RUN_DAILY"
)

mkdir -p "$(dirname "$OUT")"
exec > >(tee "$OUT") 2>&1

fail() {
  echo "EE3 RESULT=FAIL ($1)"
  echo "[DEBUG] --- device/app ---"
  { adb get-state && adb shell pm path "$PKG"; } 2>&1 | tr -d $'\r' || true
  echo "[DEBUG] --- deviceidle (head) ---"
  adb shell dumpsys deviceidle 2>/dev/null | head -n 60 | tr -d $'\r' || true
  echo "[DEBUG] --- jobscheduler (pkg slice) ---"
  adb shell dumpsys jobscheduler 2>/dev/null | tr -d $'\r' | awk -v p="$PKG" 'BEGIN{RS="\nJOB ";FS="\n";IGNORECASE=1} $0~p {print "JOB " $0 "\n----"}' | head -n 200 || true
  echo "[DEBUG] --- WorkManager (pkg slice) ---"
  adb shell dumpsys activity service WorkManager 2>/dev/null | tr -d $'\r' | awk -v p="$PKG" 'BEGIN{IGNORECASE=1} $0~p && ($0~/Movement|Intensity|Worker|ENQUEUED|RUNNING|SUCCEEDED|FAILED|RETRY/){print}' | head -n 200 || true
  echo "[DEBUG] --- CSV head/tail ---"
  { adb exec-out run-as "$PKG" sh -c 'head -n 20 "'"$CSV"'" 2>/dev/null | tr -d "\r"'; echo "----"; adb exec-out run-as "$PKG" sh -c 'tail -n 20 "'"$CSV"'" 2>/dev/null | tr -d "\r"'; } || echo "<missing>"
  echo "[DEBUG] --- logcat tail (filtered) ---"
  adb logcat -d -v brief 2>/dev/null | grep -Ei "$PKG|MovementIntensity|IntensityWorker|MovementIntensityDailyWorker|WorkManager|TriggerReceiver|enqueue|SUCCEEDED|Succeeded|Completed|Finished" | tail -n 200 | tr -d $'\r' || true
  exit 1
}

echo "[INFO] EE3 — Runs under Doze/idle (fast 15s)"

adb get-state >/dev/null 2>&1 || { echo "EE3 RESULT=FAIL (no device)"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "EE3 RESULT=FAIL (app not installed)"; exit 3; }

TODAY="$(adb shell 'toybox date +%F 2>/dev/null || date +%F' | tr -d $'\r')"
[ -n "$TODAY" ] || fail "could not read device date"

echo "[INFO] Forcing device into idle..."
{ adb shell cmd deviceidle force-idle 2>/dev/null || true; adb shell dumpsys deviceidle force-idle 2>/dev/null || true; } >/dev/null 2>&1
sleep 1

echo "[INFO] Clear logcat; trigger MovementIntensity broadcasts..."
adb logcat -c >/dev/null 2>&1 || true
for a in "${ACTIONS[@]}"; do
  adb shell cmd activity broadcast -n "$RCV" -a "$a" --receiver-foreground --user 0 >/dev/null 2>&1 || true
  adb shell am broadcast -a "$a" >/dev/null 2>&1 || true
done

deadline=$((SECONDS+15))
FOUND_CSV=""
FOUND_LOG=""
HDR=""
while (( SECONDS < deadline )); do
  HDR="$(adb exec-out run-as "$PKG" sh -c 'sed -n "1p" "'"$CSV"'" 2>/dev/null | tr -d "\r"' || true)"
  if [ "$HDR" = "date,intensity" ]; then
    FOUND_CSV="$(adb exec-out run-as "$PKG" awk -F, -v d="$TODAY" '
      NR==1{next}
      $1==d {
        gsub(/^[[:space:]]+|[[:space:]]+$/,"",$2);
        if ($2 ~ /^-?[0-9]+(\.[0-9]+)?$/) { v=$2+0; if (v>=0 && v<=100) { print $0; exit } }
      }' "'"$CSV"'" 2>/dev/null | tr -d "\r" || true)"
    [ -n "$FOUND_CSV" ] && break
  fi
  FOUND_LOG="$(adb logcat -d -v brief 2>/dev/null | grep -E 'MovementIntensity|IntensityWorker|WorkManager' | grep -Ei 'SUCCEEDED|Succeeded|Finished|Completed' | tail -n 1 | tr -d $'\r' || true)"
  [ -n "$FOUND_LOG" ] && break
  sleep 1
done

echo "csv_path=$CSV"
echo "header=${HDR:-<none>}"
echo "today=$TODAY"
echo "csv_match=${FOUND_CSV:-<none>}"
echo "log_success_sample=${FOUND_LOG:-<none>}"
echo "--- CSV head ---"
adb exec-out run-as "$PKG" sh -c 'head -n 20 "'"$CSV"'" 2>/dev/null | tr -d "\r"' || true
echo "--- logcat (tail, filtered) ---"
adb logcat -d -v brief 2>/dev/null | grep -Ei "$PKG|MovementIntensity|IntensityWorker|MovementIntensityDailyWorker|WorkManager|TriggerReceiver|enqueue|SUCCEEDED|Succeeded|Completed|Finished" | tail -n 200 | tr -d $'\r' || true

if [ -z "$FOUND_CSV" ] && [ -z "$FOUND_LOG" ]; then
  fail "no CSV row and no SUCCESS log observed under idle within 15s"
fi

echo "EE3 RESULT=PASS"
exit 0
