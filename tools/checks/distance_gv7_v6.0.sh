#!/bin/bash
set -euo pipefail

PKG="com.nick.myrecoverytracker"
ACTION="com.nick.myrecoverytracker.ACTION_RUN_DISTANCE_DAILY"
CSV_CANDS=("files/daily_distance_log.csv" "files/daily_distance.csv")
OUT="evidence/v6.0/distance/gv7.txt"
mkdir -p "$(dirname "$OUT")"

fail(){
  echo "GV7 RESULT=FAIL ($1)" | tee "$OUT"
  echo "--- DEBUG: device date/time ---" | tee -a "$OUT"
  (adb shell 'toybox date "+%F %T %Z %z"' 2>/dev/null || date "+%F %T %Z %z") | tr -d $'\r' | tee -a "$OUT"
  echo "--- DEBUG: CSV candidates (head) ---" | tee -a "$OUT"
  for c in "${CSV_CANDS[@]}"; do
    echo "[$c]" | tee -a "$OUT"
    adb exec-out run-as "$PKG" sh -c 'head -n 5 "'"$c"'" 2>/dev/null | tr -d "\r" || echo "<missing>"' | tee -a "$OUT"
  done
  echo "--- DEBUG: logcat (tail Distance) ---" | tee -a "$OUT"
  adb logcat -d | grep -E "DistanceWorker|TriggerReceiver|ACTION_RUN_DISTANCE_DAILY" | tail -n 80 | tr -d $'\r' | tee -a "$OUT" || true
  exit 1
}

adb get-state >/dev/null 2>&1 || { echo "GV7 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "GV7 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

TODAY="$(adb shell 'toybox date +%F' 2>/dev/null | tr -d $'\r' || date +%F)"
EPOCH="$(adb shell 'toybox date +%s' 2>/dev/null | tr -d $'\r' || date +%s)"
YESTERDAY="$(
  if adb shell 'toybox date -r 0 +%F' >/dev/null 2>&1; then
    adb shell "toybox date -r $(( (${EPOCH:-0}) - 86400 )) +%F" 2>/dev/null | tr -d $'\r'
  else
    date -v-1d +%F 2>/dev/null || date -d 'yesterday' +%F 2>/dev/null || python - <<'PY'
from datetime import datetime, timedelta; print((datetime.utcnow()-timedelta(days=1)).strftime("%Y-%m-%d"))
PY
  fi
)"

CSV_PATH=""
HDR=""
for c in "${CSV_CANDS[@]}"; do
  H="$(adb exec-out run-as "$PKG" sh -c 'sed -n "1p" "'"$c"'" 2>/dev/null | tr -d "\r"' || true)"
  if [ -n "$H" ]; then CSV_PATH="$c"; HDR="$H"; break; fi
done
[ -n "$CSV_PATH" ] || fail "no daily distance CSV readable on device"
[ -n "$HDR" ] || fail "empty header in $CSV_PATH"

adb logcat -c >/dev/null 2>&1 || true
adb shell am broadcast -a "$ACTION" -n "$PKG/.TriggerReceiver" >/dev/null 2>&1 || true
adb shell am broadcast -a "$ACTION" >/dev/null 2>&1 || true

deadline=$(( $(date +%s) + 12 ))
FOUND_LINE=""
while [ $(date +%s) -le $deadline ]; do
  TAIL="$(adb exec-out run-as "$PKG" sh -c 'tail -n 120 "'"$CSV_PATH"'" 2>/dev/null | tr -d "\r"' || true)"
  case "$HDR" in
    "date,distance_km")
      FOUND_LINE="$(printf "%s\n" "$TAIL" | awk -F, -v t="$TODAY" -v y="$YESTERDAY" '
        $1==t || $1==y {
          if ($2 ~ /^([0-9]+)(\.[0-9]+)?$/) {
            v=$2+0; if (v>=0 && v<=200.0) { print $0; exit }
          }
        }')"
      ;;
    "date,meters")
      FOUND_LINE="$(printf "%s\n" "$TAIL" | awk -F, -v t="$TODAY" -v y="$YESTERDAY" '
        $1==t || $1==y {
          if ($2 ~ /^[0-9]+$/) { v=$2+0; if (v>=0 && v<=200000) { print $0; exit } }
        }')"
      ;;
    "date,meters,segments")
      FOUND_LINE="$(printf "%s\n" "$TAIL" | awk -F, -v t="$TODAY" -v y="$YESTERDAY" '
        $1==t || $1==y {
          ok1=($2 ~ /^[0-9]+$/); ok2=($3 ~ /^[0-9]+$/);
          if (ok1){ m=$2+0; if (m<0 || m>200000) ok1=0 }
          if (ok2){ s=$3+0; if (s<0 || s>5000) ok2=0 }
          if (ok1 && ok2) { print $0; exit }
        }')"
      ;;
    *)
      fail "unsupported header '$HDR' in $CSV_PATH"
      ;;
  esac
  [ -n "$FOUND_LINE" ] && break
  sleep 1
done

{
  echo "csv_path=$CSV_PATH"
  echo "header=$HDR"
  echo "today=$TODAY"
  echo "yesterday=$YESTERDAY"
  echo "matched_row=${FOUND_LINE:-<none>}"
  echo "--- CSV tail (last 60) ---"
  adb exec-out run-as "$PKG" sh -c 'tail -n 60 "'"$CSV_PATH"'" 2>/dev/null | tr -d "\r"' || true
  echo "--- logcat (DistanceWorker tail) ---"
  adb logcat -d | grep -E "DistanceWorker|TriggerReceiver|ACTION_RUN_DISTANCE_DAILY" | tail -n 80 | tr -d $'\r' || true
} | tee "$OUT" >/dev/null

[ -n "$FOUND_LINE" ] || fail "no valid row for today/yesterday after manual trigger"

echo "GV7 RESULT=PASS" | tee -a "$OUT"
exit 0
