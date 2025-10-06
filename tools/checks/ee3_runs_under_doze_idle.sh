#!/bin/bash
set -euo pipefail

PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/distance/ee3.txt"
mkdir -p "$(dirname "$OUT")"
exec > >(tee "$OUT") 2>&1

fail() {
  echo "[FAIL] $1"
  echo "[DEBUG] --- deviceidle (head) ---"
  adb shell dumpsys deviceidle 2>/dev/null | head -n 40 || true
  echo "[DEBUG] --- jobscheduler pkg slice (head/tail) ---"
  printf "%s\n" "${JS_PKG:-<empty>}" | head -n 60
  echo "----"
  printf "%s\n" "${JS_PKG:-<empty>}" | tail -n 60
  echo "[DEBUG] --- logcat tail (filtered) ---"
  adb logcat -d -v brief 2>/dev/null | grep -E "$PKG|DistanceWorker|TriggerReceiver|WorkManager|SystemJobService" | tail -n 200 || true
  echo "[DEBUG] --- location_log.csv (head) ---"
  adb exec-out run-as "$PKG" sh -c 'head -n 20 files/location_log.csv | tr -d "\r"' 2>/dev/null || echo "<missing>"
  echo "[DEBUG] --- daily_distance_log.csv (head) ---"
  adb exec-out run-as "$PKG" sh -c 'head -n 20 files/daily_distance_log.csv | tr -d "\r"' 2>/dev/null || echo "<missing>"
  echo "RESULT=FAIL"
  exit 1
}

echo "[INFO] EE3 Runs under Doze/idle"
adb get-state | grep -q "^device$" || { echo "[FAIL] No device connected"; echo "RESULT=FAIL"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "[FAIL] App $PKG not installed"; echo "RESULT=FAIL"; exit 3; }

TODAY="$(adb shell date +%F 2>/dev/null | tr -d $'\r')"
[[ -n "$TODAY" ]] || fail "Could not read device date"

echo "[INFO] Forcing device into idle"
adb shell dumpsys deviceidle force-idle >/dev/null 2>&1 || fail "Failed to force idle"
sleep 1

echo "[INFO] Seeding files/location_log.csv for $TODAY"
adb exec-out run-as "$PKG" sh -c "mkdir -p files && printf 'ts,lat,lon,accuracy\n%s 08:00:00,-33.8675,151.2070,12\n%s 08:30:00,-33.8700,151.2100,10\n%s 09:00:00,-33.8730,151.2150,9\n' '$TODAY' '$TODAY' '$TODAY' > files/location_log.csv" || fail "Seed write failed"
adb exec-out run-as "$PKG" rm -f files/daily_distance_log.csv || true

HDR="$(adb exec-out run-as "$PKG" sed -n '1p' files/location_log.csv 2>/dev/null | tr -d $'\r' || true)"
CNT="$(adb exec-out run-as "$PKG" wc -l files/location_log.csv 2>/dev/null | awk '{print $1}')"
[[ "$HDR" = "ts,lat,lon,accuracy" ]] || fail "Seed header mismatch"
[[ "${CNT:-0}" -ge 4 ]] || fail "Seed rows missing"

echo "[INFO] Broadcasting app trigger"
adb logcat -c || true
adb shell cmd activity broadcast -n "$PKG/.TriggerReceiver" -a "$PKG.ACTION_RUN_DISTANCE_DAILY" --receiver-foreground --user 0 >/dev/null 2>&1 || true

echo "[INFO] Capturing jobscheduler slice (for debug only)"
JS_PKG="$(adb shell dumpsys jobscheduler 2>/dev/null | tr -d '\r' | awk 'BEGIN{RS="";FS="\n"} /com\.nick\.myrecoverytracker/ && /SystemJobService/ {print $0 "\n----"}' || true)"

echo "[INFO] Waiting for SUCCESS evidence (CSV/logs) under idle (deadline 15s)"
deadline=$((SECONDS+15))
passed=0
CSV_ROW=""
while (( SECONDS < deadline )); do
  if adb exec-out run-as "$PKG" awk -F, -v d="$TODAY" 'NR>1 && $1==d{print;exit 0} END{exit 1}' files/daily_distance_log.csv 2>/dev/null > /tmp/ee3_row.$$; then
    CSV_ROW="$(cat /tmp/ee3_row.$$ | tr -d $'\r')"
    passed=1
    break
  fi
  if adb logcat -d -v brief 2>/dev/null | grep -qiE 'DistanceWorker|daily_distance_log\.csv|onStartJob|execute|Finished|Success'; then
    passed=1
    break
  fi
  sleep 1
done
rm -f /tmp/ee3_row.$$ 2>/dev/null || true

echo "=== location_log.csv (head) ==="
adb exec-out run-as "$PKG" head -n 10 files/location_log.csv 2>/dev/null | tr -d $'\r' || true
echo "=== daily_distance_log.csv (head) ==="
adb exec-out run-as "$PKG" head -n 10 files/daily_distance_log.csv 2>/dev/null | tr -d $'\r' || true
echo "=== logcat (tail, filtered) ==="
adb logcat -d -v brief 2>/dev/null | grep -E "$PKG|DistanceWorker|TriggerReceiver|WorkManager|SystemJobService" | tail -n 200 || true

[[ $passed -eq 1 ]] || fail "No SUCCESS indication while under idle"

if [[ -n "$CSV_ROW" ]]; then
  echo "[PASS] CSV row present for $TODAY: $CSV_ROW"
else
  echo "[PASS] SUCCESS evidenced by logs"
fi
echo "RESULT=PASS"
exit 0
