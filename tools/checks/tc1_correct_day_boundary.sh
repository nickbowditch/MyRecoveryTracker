#!/bin/bash
set -euo pipefail

PKG="com.nick.myrecoverytracker"
APP_CSV="files/daily_distance_log.csv"
OUT="evidence/v6.0/distance/tc1.txt"
mkdir -p "$(dirname "$OUT")"
exec > >(tee "$OUT") 2>&1

fail() {
  echo "[FAIL] $1"
  echo "[DEBUG] --- device date/time (local) ---"
  (adb shell date '+%F %T %Z %z' 2>/dev/null || true) | tr -d $'\r'
  echo "[DEBUG] --- app files dir ---"
  adb exec-out run-as "$PKG" sh -c 'pwd; ls -la' 2>/dev/null || true
  echo "[DEBUG] --- location_log.csv (head) ---"
  adb exec-out run-as "$PKG" sh -c 'head -n 20 files/location_log.csv | tr -d "\r"' 2>/dev/null || echo "<missing>"
  echo "[DEBUG] --- CSV exists? / size ---"
  adb exec-out run-as "$PKG" sh -c '[ -f "'"$APP_CSV"'" ] && { ls -l "'"$APP_CSV"'"; wc -l "'"$APP_CSV"'"; } || echo "<missing>"' 2>/dev/null || true
  echo "[DEBUG] --- CSV head ---"
  adb exec-out run-as "$PKG" sh -c 'head -n 20 "'"$APP_CSV"'" | tr -d "\r"' 2>/dev/null || echo "<missing>"
  echo "[DEBUG] --- CSV tail ---"
  adb exec-out run-as "$PKG" sh -c 'tail -n 20 "'"$APP_CSV"'" | tr -d "\r"' 2>/dev/null || echo "<missing>"
  echo "[DEBUG] --- distinct dates & counts (safe) ---"
  adb exec-out run-as "$PKG" sh -c '[ -f "'"$APP_CSV"'" ] && awk -F, '\''NR>1 && $1~/^[0-9]{4}-[0-9]{2}-[0-9]{2}$/ {c[$1]++} END{for(d in c) printf "%s,%d\n",d,c[d]}'\'' "'"$APP_CSV"'" | sort || true' 2>/dev/null || true
  echo "[DEBUG] --- WorkManager / JobScheduler hints ---"
  adb shell dumpsys jobscheduler 2>/dev/null | tr -d '\r' | awk 'BEGIN{RS="";FS="\n"} /com\.nick\.myrecoverytracker/ && /androidx\.work\.impl\.background\.systemjob\.SystemJobService/ {print "----\n" $0 "\n----"}' | sed -n '1,120p' || true
  echo "[DEBUG] --- logcat tail (Distance/WorkManager) ---"
  adb logcat -d -v brief 2>/dev/null | grep -E "$PKG|DistanceWorker|TriggerReceiver|WorkManager|SystemJobService" | tail -n 200 || true
  echo "[DEBUG] --- computed dates ---"
  echo "last_date=${LAST_DATE:-<none>} today=${TODAY:-<unk>} yesterday=${YESTERDAY:-<unk>} tomorrow=${TOMORROW:-<unk>}"
  echo "RESULT=FAIL"
  exit 1
}

echo "[INFO] TC1 Correct day boundary — one row per local day (±1 day tolerance)"

echo "[INFO] Checking device and app..."
adb get-state | grep -q "^device$" || fail "No device connected"
adb shell pm path "$PKG" >/dev/null 2>&1 || fail "App $PKG not installed"

echo "[INFO] Reading device-local dates..."
TODAY="$(adb shell 'toybox date +%F 2>/dev/null || date +%F' | tr -d $'\r')"
YESTERDAY="$(adb shell 'toybox date -d "-1 day" +%F 2>/dev/null || date -d "yesterday" +%F' 2>/dev/null | tr -d $'\r' || true)"
TOMORROW="$(adb shell 'toybox date -d "+1 day" +%F 2>/dev/null || date -d "tomorrow" +%F' 2>/dev/null | tr -d $'\r' || true)"
[[ -n "$TODAY" ]] || fail "Could not determine device date"

echo "[INFO] Verifying CSV presence and header..."
if ! adb exec-out run-as "$PKG" sh -c '[ -f "'"$APP_CSV"'" ]' 2>/dev/null; then
  fail "CSV not found at $APP_CSV"
fi
HDR="$(adb exec-out run-as "$PKG" sed -n '1p' "$APP_CSV" 2>/dev/null | tr -d $'\r' || true)"
[[ "$HDR" = "date,distance_km" ]] || fail "Header mismatch (want: date,distance_km; got: ${HDR:-<none>})"

echo "[INFO] Ensuring one row per date (no duplicates)..."
DUPS="$(adb exec-out run-as "$PKG" awk -F, 'NR>1 && $1~/^[0-9]{4}-[0-9]{2}-[0-9]{2}$/ {c[$1]++} END{for (d in c) if (c[d]>1) printf "%s,%d\n",d,c[d]}' "$APP_CSV" 2>/dev/null | tr -d $'\r' || true)"
[[ -z "${DUPS// /}" ]] || fail "Duplicate date rows found"

echo "[INFO] Checking last row date against local day ±1..."
LAST_DATE="$(adb exec-out run-as "$PKG" awk -F, 'NR>1 && $1~/^[0-9]{4}-[0-9]{2}-[0-9]{2}$/ {ld=$1} END{if(ld!="") print ld}' "$APP_CSV" 2>/dev/null | tr -d $'\r' || true)"
[[ -n "$LAST_DATE" ]] || fail "No data rows present in CSV"

if [[ "$LAST_DATE" != "$TODAY" && "$LAST_DATE" != "$YESTERDAY" && "$LAST_DATE" != "$TOMORROW" ]]; then
  fail "Last row date $LAST_DATE is outside tolerance of device local day"
fi

echo "[PASS] One row per date (no duplicates) and last row aligns with local day (±1)."
echo "RESULT=PASS"
exit 0
