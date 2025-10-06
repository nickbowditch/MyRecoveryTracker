#!/bin/bash
set -euo pipefail

PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/movement_intensity/ee3.txt"
CSV="files/daily_movement_intensity.csv"
RCV="$PKG/.TriggerReceiver"
ACTIONS=(
  "$PKG.ACTION_RUN_MOVEMENT_INTENSITY_DAILY"
  "$PKG.ACTION_RUN_MOVEMENT_DAILY"
  "$PKG.ACTION_RUN_INTENSITY_DAILY"
  "$PKG.ACTION_RUN_DAILY"
)
mkdir -p "$(dirname "$OUT")"

fail() {
  echo "EE3 RESULT=FAIL ($1)" | tee "$OUT"
  echo "--- DEBUG: device/app ---" | tee -a "$OUT"
  { adb get-state && adb shell pm path "$PKG"; } 2>&1 | tr -d $'\r' | tee -a "$OUT" || true
  echo "--- DEBUG: deviceidle (head) ---" | tee -a "$OUT"
  adb shell dumpsys deviceidle 2>/dev/null | head -n 60 | tr -d $'\r' | tee -a "$OUT" || true
  echo "--- DEBUG: jobscheduler pkg slice ---" | tee -a "$OUT"
  adb shell dumpsys jobscheduler 2>/dev/null | tr -d $'\r' | awk -v p="$PKG" 'BEGIN{RS="JOB ";FS="\n";IGNORECASE=1} $0 ~ p && ($0 ~ /Movement|Intensity|RTC|READY|RUNNABLE/){print "JOB " $0 "\n----"}' | sed -n '1,160p' | tee -a "$OUT" || true
  echo "--- DEBUG: WorkManager pkg slice ---" | tee -a "$OUT"
  adb shell dumpsys activity service WorkManager 2>/dev/null | tr -d $'\r' | awk -v p="$PKG" 'BEGIN{IGNORECASE=1} ($0 ~ p) && ($0 ~ /Movement|Intensity|Worker|RUNNING|SUCCEEDED|ENQUEUED/){print}' | sed -n '1,160p' | tee -a "$OUT" || true
  echo "--- DEBUG: CSV head/tail ---" | tee -a "$OUT"
  adb exec-out run-as "$PKG" sh -c 'head -n 12 "'"$CSV"'" 2>/dev/null | tr -d "\r"' | tee -a "$OUT" || true
  adb exec-out run-as "$PKG" sh -c 'tail -n 12 "'"$CSV"'" 2>/dev/null | tr -d "\r"' | tee -a "$OUT" || true
  echo "--- DEBUG: logcat tail (filtered) ---" | tee -a "$OUT"
  adb logcat -d -v brief 2>/dev/null | grep -Ei "$PKG|MovementIntensity|IntensityWorker|Movement|WorkManager|TriggerReceiver|enqueue|SUCCEEDED" | tail -n 200 | tr -d $'\r' | tee -a "$OUT" || true
  exit 1
}

echo "[INFO] EE3 — Runs under Doze/idle" | tee "$OUT" >/dev/null

adb get-state >/dev/null 2>&1 || { echo "EE3 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "EE3 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

TODAY="$(adb shell 'toybox date +%F 2>/dev/null || date +%F' | tr -d $'\r')"
YDAY="$(adb shell 'toybox date -d "-1 day" +%F 2>/dev/null || date -d "yesterday" +%F' 2>/dev/null | tr -d $'\r' || true)"
[ -n "$TODAY" ] || fail "could not read device date"

adb shell dumpsys deviceidle force-idle >/dev/null 2>&1 || true
sleep 1

adb logcat -c >/dev/null 2>&1 || true
for a in "${ACTIONS[@]}"; do
  adb shell cmd activity broadcast -n "$RCV" -a "$a" --receiver-foreground --user 0 >/dev/null 2>&1 || true
  adb shell am broadcast -a "$a" >/dev/null 2>&1 || true
done

deadline=$((SECONDS+20))
FOUND_LINE=""
HDR=""
while (( SECONDS < deadline )); do
  HDR="$(adb exec-out run-as "$PKG" sh -c 'sed -n "1p" "'"$CSV"'" 2>/dev/null | tr -d "\r"' || true)"
  if [ "$HDR" = "date,intensity" ]; then
    FOUND_LINE="$(adb exec-out run-as "$PKG" awk -F, -v t="$TODAY" -v y="$YDAY" '
      NR==1{next}
      $1==t || $1==y {
        gsub(/^[[:space:]]+|[[:space:]]+$/,"",$2);
        if ($2 ~ /^-?[0-9]+(\.[0-9]+)?$/) {
          v=$2+0; if (v>=0 && v<=100) { print $0; exit }
        }
      }' "'"$CSV"'" 2>/dev/null | tr -d "\r" || true)"
    [ -n "$FOUND_LINE" ] && break
  fi
  sleep 1
done

{
  echo "csv_path=$CSV"
  echo "header=${HDR:-<none>}"
  echo "today=$TODAY"
  echo "yesterday=$YDAY"
  echo "matched_row=${FOUND_LINE:-<none>}"
  echo "--- CSV tail (last 60) ---"
  adb exec-out run-as "$PKG" sh -c 'tail -n 60 "'"$CSV"'" 2>/dev/null | tr -d "\r"' || true
  echo "--- logcat (Movement/Intensity tail) ---"
  adb logcat -d -v brief 2>/dev/null | grep -Ei "$PKG|MovementIntensity|IntensityWorker|Movement|WorkManager|TriggerReceiver|enqueue|SUCCEEDED" | tail -n 200 | tr -d $'\r' || true
} | tee -a "$OUT" >/dev/null

[ -n "$FOUND_LINE" ] || fail "no valid today/yesterday row observed under idle"

echo "EE3 RESULT=PASS" | tee -a "$OUT"
exit 0
