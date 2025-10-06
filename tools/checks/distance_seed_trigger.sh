#!/bin/bash
set -euo pipefail

PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/distance/seed_trigger.txt"
mkdir -p "$(dirname "$OUT")"
exec > >(tee "$OUT") 2>&1

fail() {
  echo "[FAIL] $1"
  echo "[DEBUG] deviceidle (head)"
  adb shell dumpsys deviceidle 2>/dev/null | head -n 20 || true
  echo "[DEBUG] files/ listing"
  adb exec-out run-as "$PKG" ls -la 2>/dev/null || echo "<run-as ls failed>"
  echo "[DEBUG] location_log.csv head"
  adb exec-out run-as "$PKG" sh -c 'head -n 20 files/location_log.csv | tr -d "\r"' 2>/dev/null || echo "<missing>"
  echo "[DEBUG] daily_distance_log.csv head"
  adb exec-out run-as "$PKG" sh -c 'head -n 20 files/daily_distance_log.csv | tr -d "\r"' 2>/dev/null || echo "<missing>"
  echo "[DEBUG] logcat tail (filtered)"
  adb logcat -d -v brief 2>/dev/null | grep -E "$PKG|DistanceWorker|TriggerReceiver|WorkManager|SystemJobService" | tail -n 200 || true
  echo "RESULT=FAIL"
  exit 1
}

echo "[INFO] Seed & Trigger Distance worker"
adb get-state | grep -q "^device$" || fail "No device connected"
adb shell pm path "$PKG" >/dev/null 2>&1 || fail "App $PKG not installed"

TODAY="$(adb shell date +%F 2>/dev/null | tr -d $'\r')"
[[ -n "$TODAY" ]] || fail "Could not read device date"

echo "[INFO] Seeding files/location_log.csv for $TODAY"
adb exec-out run-as "$PKG" sh -c "mkdir -p files && printf 'ts,lat,lon,accuracy\n%s 08:00:00,-33.8675,151.2070,12\n%s 08:30:00,-33.8700,151.2100,10\n%s 09:00:00,-33.8730,151.2150,9\n' '$TODAY' '$TODAY' '$TODAY' > files/location_log.csv" || fail "Seed write failed"

echo "[INFO] Resetting daily_distance_log.csv"
adb exec-out run-as "$PKG" rm -f files/daily_distance_log.csv || true

HDR="$(adb exec-out run-as "$PKG" sed -n '1p' files/location_log.csv 2>/dev/null | tr -d $'\r' || true)"
CNT="$(adb exec-out run-as "$PKG" wc -l files/location_log.csv 2>/dev/null | awk '{print $1}')"
[[ "$HDR" = "ts,lat,lon,accuracy" ]] || fail "Seed header mismatch"
[[ "${CNT:-0}" -ge 4 ]] || fail "Seed rows missing"

echo "[INFO] Triggering broadcast"
adb logcat -c || true
adb shell cmd activity broadcast -n "$PKG/.TriggerReceiver" -a "$PKG.ACTION_RUN_DISTANCE_DAILY" --receiver-foreground --user 0 >/dev/null 2>&1 || true

echo "[INFO] Waiting for worker evidence (max 10s)"
deadline=$((SECONDS+10))
found=0
while (( SECONDS < deadline )); do
  if adb exec-out run-as "$PKG" awk -F, -v d="$TODAY" 'NR>1 && $1==d{exit 0} END{exit 1}' files/daily_distance_log.csv 2>/dev/null; then
    found=1
    break
  fi
  if adb logcat -d -v brief 2>/dev/null | grep -qi "DistanceWorker"; then
    found=1
    break
  fi
  sleep 1
done

echo "=== location_log.csv (head) ==="
adb exec-out run-as "$PKG" head -n 10 files/location_log.csv 2>/dev/null | tr -d $'\r' || true
echo "=== daily_distance_log.csv (head) ==="
adb exec-out run-as "$PKG" head -n 10 files/daily_distance_log.csv 2>/dev/null | tr -d $'\r' || true
echo "=== logcat (tail, filtered) ==="
adb logcat -d -v brief 2>/dev/null | grep -E "$PKG|DistanceWorker|TriggerReceiver|WorkManager" | tail -n 200 || true

[[ $found -eq 1 ]] || fail "No worker evidence (no CSV row for $TODAY and no DistanceWorker log)"

CSV_HDR="$(adb exec-out run-as "$PKG" sed -n '1p' files/daily_distance_log.csv 2>/dev/null | tr -d $'\r' || true)"
CSV_ROW="$(adb exec-out run-as "$PKG" awk -F, -v d="$TODAY" 'NR>1 && $1==d{print;exit}' files/daily_distance_log.csv 2>/dev/null | tr -d $'\r' || true)"
[[ "$CSV_HDR" = "date,distance_km" ]] || fail "daily_distance_log.csv header missing"
[[ -n "$CSV_ROW" ]] || fail "No row for $TODAY in daily_distance_log.csv"

echo "[PASS] Seeded, triggered, and found $TODAY row: $CSV_ROW"
echo "RESULT=PASS"
exit 0
