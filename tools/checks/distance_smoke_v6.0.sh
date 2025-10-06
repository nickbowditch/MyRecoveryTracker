#!/bin/sh
set -eu

PKG="com.nick.myrecoverytracker"
RCV="$PKG/.TriggerReceiver"
ACT="$PKG.ACTION_RUN_DISTANCE_DAILY"
LOC="files/location_log.csv"
DAY="files/daily_distance_log.csv"
OUT="evidence/v6.0/distance/smoke.txt"
mkdir -p "$(dirname "$OUT")"

fail() {
  echo "[FAIL] $1"
  echo "[DEBUG] device time"
  adb shell date '+%F %T %Z %z' 2>/dev/null | tr -d '\r' || true
  echo "[DEBUG] app files list"
  adb exec-out run-as "$PKG" sh -c 'ls -l files | tr -d "\r"' 2>/dev/null || true
  echo "[DEBUG] location_log.csv (head)"
  adb exec-out run-as "$PKG" sh -c 'head -n 10 "'"$LOC"'" | tr -d "\r"' 2>/dev/null || true
  echo "[DEBUG] daily_distance_log.csv (head)"
  adb exec-out run-as "$PKG" sh -c 'head -n 10 "'"$DAY"'" | tr -d "\r"' 2>/dev/null || true
  echo "[DEBUG] logcat tail (filtered)"
  adb logcat -d -v brief 2>/dev/null | grep -E "$PKG|DistanceWorker|TriggerReceiver|WorkManager|SystemJobService" | tail -n 200 || true
  echo "RESULT=FAIL"
  exit 1
}

echo "[INFO] Distance smoke — seed, trigger, verify"
adb get-state | grep -q "^device$" || { mkdir -p "$(dirname "$OUT")"; echo "RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

TODAY="$(adb shell 'toybox date +%F 2>/dev/null || date +%F' | tr -d '\r')"
[ -n "$TODAY" ] || fail "could not read device date"

echo "[INFO] Seed minimal location_log.csv for $TODAY"
adb exec-out run-as "$PKG" sh -c "mkdir -p files && printf 'ts,lat,lon,accuracy\n%s 08:00:00,-33.8675,151.2070,12\n%s 08:30:00,-33.8700,151.2100,10\n' '$TODAY' '$TODAY' > '$LOC'" || fail "seed write failed"
adb exec-out run-as "$PKG" rm -f "$DAY" || true

echo "[INFO] Broadcast trigger"
adb logcat -c || true
adb shell cmd activity broadcast -n "$RCV" -a "$ACT" --receiver-foreground --user 0 >/dev/null 2>&1 || true

echo "[INFO] Wait for output (deadline 15s)"
deadline=$(( $(date +%s) + 15 ))
saw_csv=0
while [ "$(date +%s)" -lt "$deadline" ]; do
  HDR="$(adb exec-out run-as "$PKG" sed -n '1p' "$DAY" 2>/dev/null | tr -d '\r' || true)"
  if [ "$HDR" = "date,distance_km" ]; then
    ROW="$(adb exec-out run-as "$PKG" awk -F, -v d="$TODAY" 'NR>1&&$1==d{print;exit}' "$DAY" 2>/dev/null | tr -d '\r' || true)"
    if [ -n "$ROW" ]; then
      saw_csv=1
      break
    fi
  fi
  sleep 1
done

{
  echo "HEADER=$(adb exec-out run-as "$PKG" sed -n '1p' "$DAY" 2>/dev/null | tr -d '\r' || echo "<missing>")"
  echo "ROW_TODAY=$(adb exec-out run-as "$PKG" awk -F, -v d="$TODAY" 'NR>1&&$1==d{print;exit}' "$DAY" 2>/dev/null | tr -d '\r' || echo "<none>")"
  echo "--- location_log.csv (head) ---"
  adb exec-out run-as "$PKG" head -n 10 "$LOC" 2>/dev/null | tr -d '\r' || true
  echo "--- daily_distance_log.csv (head) ---"
  adb exec-out run-as "$PKG" head -n 10 "$DAY" 2>/dev/null | tr -d '\r' || true
  echo "--- logcat tail (filtered) ---"
  adb logcat -d -v brief 2>/dev/null | grep -E "$PKG|DistanceWorker|TriggerReceiver|WorkManager|SystemJobService" | tail -n 200 || true
} | tee "$OUT" >/dev/null

[ "$saw_csv" -eq 1 ] && { echo "[PASS] Found today row in daily_distance_log.csv" | tee -a "$OUT"; echo "RESULT=PASS"; exit 0; }

fail "daily_distance_log.csv missing or no row for $TODAY"
