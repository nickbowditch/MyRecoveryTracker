#!/bin/sh
set -eu

PKG="com.nick.myrecoverytracker"
LOC="files/location_log.csv"
DAY="files/daily_distance_log.csv"
RCV="$PKG/.TriggerReceiver"
ACT="$PKG.ACTION_RUN_DISTANCE_DAILY"
OUT="evidence/v6.0/distance/diagnose.txt"
mkdir -p "$(dirname "$OUT")"

fail() {
  echo "[FAIL] $1"
  echo "[DEBUG] device time"
  adb shell date '+%F %T %Z %z' 2>/dev/null | tr -d '\r' || true
  echo "[DEBUG] package + receiver resolve"
  adb shell dumpsys package "$PKG" 2>/dev/null | tr -d '\r' | sed -n '1,160p' || true
  adb shell cmd package query-intent-receivers -a "$ACT" 2>/dev/null | tr -d '\r' || true
  echo "[DEBUG] app files dir"
  adb exec-out run-as "$PKG" sh -c 'pwd; ls -la' 2>/dev/null || true
  echo "[DEBUG] location_log.csv (head/tail)"
  adb exec-out run-as "$PKG" sh -c 'head -n 10 "'"$LOC"'" 2>/dev/null | tr -d "\r" || echo "<missing>"'
  adb exec-out run-as "$PKG" sh -c 'tail -n 10 "'"$LOC"'" 2>/dev/null | tr -d "\r" || echo "<missing>"'
  echo "[DEBUG] daily_distance_log.csv (head/tail)"
  adb exec-out run-as "$PKG" sh -c 'head -n 10 "'"$DAY"'" 2>/dev/null | tr -d "\r" || echo "<missing>"'
  adb exec-out run-as "$PKG" sh -c 'tail -n 10 "'"$DAY"'" 2>/dev/null | tr -d "\r" || echo "<missing>"'
  echo "[DEBUG] WorkManager service dump (filtered)"
  adb shell dumpsys activity service WorkManager 2>/dev/null | tr -d '\r' | awk -v p="$PKG" 'BEGIN{IGNORECASE=1}/'"$PKG"'/,0{print}' | sed -n '1,160p' || true
  echo "[DEBUG] JobScheduler blocks (pkg)"
  adb shell dumpsys jobscheduler 2>/dev/null | tr -d '\r' | awk 'BEGIN{RS="";FS="\n"} /'"$PKG"'/ && /androidx\.work\.impl\.background\.systemjob\.SystemJobService/ {print "----\n"$0"\n----"}' | sed -n '1,240p' || true
  echo "[DEBUG] deviceidle allowlists (tail)"
  adb shell dumpsys deviceidle whitelist 2>/dev/null | tail -n 30 || true
  echo "[DEBUG] logcat tail (filtered)"
  adb logcat -d -v brief 2>/dev/null | grep -E "$PKG|DistanceWorker|TriggerReceiver|WorkManager|SystemJobService" | tail -n 200 || true
  echo "RESULT=FAIL"
  exit 1
}

echo "[INFO] Distance deep diagnosis"
adb get-state | grep -q "^device$" || { echo "RESULT=FAIL (no device)"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "RESULT=FAIL (app not installed)"; exit 3; }

TODAY="$(adb shell 'toybox date +%F 2>/dev/null || date +%F' | tr -d '\r')"
[ -n "$TODAY" ] || fail "could not read device date"

echo "[INFO] Ensure minimal seed for location_log.csv"
adb exec-out run-as "$PKG" sh -c "mkdir -p files && printf 'ts,lat,lon,accuracy\n%s 08:00:00,-33.8675,151.2070,12\n%s 08:30:00,-33.8700,151.2100,10\n' '$TODAY' '$TODAY' > '$LOC'" || true
adb exec-out run-as "$PKG" rm -f "$DAY" || true

echo "[INFO] Resolve receiver then broadcast"
adb shell cmd package query-intent-receivers -a "$ACT" 2>/dev/null | tr -d '\r' | tee "$OUT" >/dev/null
adb logcat -c || true
adb shell cmd activity broadcast -n "$RCV" -a "$ACT" --receiver-foreground --user 0 >/dev/null 2>&1 || true

echo "[INFO] Discover WorkManager jobs for package"
JS_RAW="$(adb shell dumpsys jobscheduler 2>/dev/null | tr -d '\r' || true)"
PKG_BLOCKS="$(printf '%s\n' "$JS_RAW" | awk 'BEGIN{RS="";FS="\n"} /'"$PKG"'/ && /androidx\.work\.impl\.background\.systemjob\.SystemJobService/ {print $0 "\n----"}' || true)"
JOB_IDS="$(printf "%s\n" "$PKG_BLOCKS" | sed -n 's/.*#u[0-9a-zA-Z]\+\/\([0-9][0-9]*\).*/\1/p' | sort -u | tr '\n' ' ' || true)"
echo "[DEBUG] job ids: ${JOB_IDS:-<none>}" | tee -a "$OUT" >/dev/null

if [ -z "${JOB_IDS// /}" ]; then
  echo "[DEBUG] no SystemJobService jobs found for WorkManager" | tee -a "$OUT" >/dev/null
fi

echo "[INFO] Try forcing each job under current state"
for J in $JOB_IDS; do
  echo "[INFO] cmd jobscheduler run -f $PKG $J" | tee -a "$OUT" >/dev/null
  set +e
  RJ="$(adb shell cmd jobscheduler run -f "$PKG" "$J" 2>&1)"
  RC=$?
  set -e
  printf "[DEBUG] run rc=%s out:\n%s\n" "$RC" "$RJ" | tee -a "$OUT" >/dev/null
done

echo "[INFO] Wait for evidence (logs or CSV) (deadline 12s)"
deadline=$((SECONDS+12))
saw_logs=0
saw_csv=0
CSV_ROW=""
while [ $SECONDS -lt $deadline ]; do
  if adb exec-out run-as "$PKG" awk -F, -v d="$TODAY" 'NR>1&&$1==d{print;exit 0} END{exit 1}' "$DAY" 2>/dev/null >/tmp/dd_row.$$; then
    CSV_ROW="$(tr -d '\r' </tmp/dd_row.$$)"
    [ -n "$CSV_ROW" ] && { saw_csv=1; break; }
  fi
  if adb logcat -d -v brief 2>/dev/null | grep -qiE 'DistanceWorker|SystemJobService|WorkManager'; then
    saw_logs=1
    break
  fi
  sleep 1
done
rm -f /tmp/dd_row.$$ 2>/dev/null || true

echo "=== app files listing ===" | tee -a "$OUT" >/dev/null
adb exec-out run-as "$PKG" sh -c 'ls -l files | tr -d "\r"' 2>/dev/null | tee -a "$OUT" >/dev/null || true
echo "=== location_log.csv (head) ===" | tee -a "$OUT" >/dev/null
adb exec-out run-as "$PKG" head -n 10 "$LOC" 2>/dev/null | tr -d '\r' | tee -a "$OUT" >/dev/null || true
echo "=== daily_distance_log.csv (head) ===" | tee -a "$OUT" >/dev/null
adb exec-out run-as "$PKG" head -n 10 "$DAY" 2>/dev/null | tr -d '\r' | tee -a "$OUT" >/dev/null || true
echo "=== logcat tail (filtered) ===" | tee -a "$OUT" >/dev/null
adb logcat -d -v brief 2>/dev/null | grep -E "$PKG|DistanceWorker|TriggerReceiver|WorkManager|SystemJobService" | tail -n 200 | tee -a "$OUT" >/dev/null || true
echo "=== jobscheduler (pkg blocks) ===" | tee -a "$OUT" >/dev/null
printf "%s\n" "$PKG_BLOCKS" | sed -n '1,240p' | tee -a "$OUT" >/dev/null

[ $saw_csv -eq 1 ] && { echo "[PASS] CSV row for $TODAY: $CSV_ROW" | tee -a "$OUT" >/dev/null; echo "RESULT=PASS"; exit 0; }

if [ $saw_logs -eq 1 ] || [ -n "${JOB_IDS// /}" ]; then
  fail "worker signaled but CSV still missing (possible app logic path not writing)"
fi

fail "no WorkManager jobs, no DistanceWorker logs, and no CSV row — trigger likely not enqueueing work"
