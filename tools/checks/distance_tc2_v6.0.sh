#!/bin/bash
set -euo pipefail

PKG="com.nick.myrecoverytracker"
LOC="files/location_log.csv"
CSV="files/daily_distance_log.csv"
RCV="$PKG/.TriggerReceiver"
ACT="$PKG.ACTION_RUN_DISTANCE_DAILY"
OUT="evidence/v6.0/distance/tc2.txt"
mkdir -p "$(dirname "$OUT")"

fail() {
  echo "TC2 RESULT=FAIL ($1)" | tee "$OUT"
  echo "--- DEBUG: device date/time (local) ---" | tee -a "$OUT"
  (adb shell 'toybox date "+%F %T %Z %z" 2>/dev/null || date "+%F %T %Z %z"') | tr -d $'\r' | tee -a "$OUT" || true
  echo "--- DEBUG: app files dir (pwd; ls -la) ---" | tee -a "$OUT"
  adb exec-out run-as "$PKG" sh -c 'pwd; ls -la' 2>/dev/null | tr -d $'\r' | tee -a "$OUT" || true
  echo "--- DEBUG: location_log.csv (head/tail) ---" | tee -a "$OUT"
  adb exec-out run-as "$PKG" sh -c 'head -n 10 "'"$LOC"'" | tr -d "\r"' 2>/dev/null | tee -a "$OUT" || true
  adb exec-out run-as "$PKG" sh -c 'tail -n 10 "'"$LOC"'" | tr -d "\r"' 2>/dev/null | tee -a "$OUT" || true
  echo "--- DEBUG: daily_distance_log.csv (head/tail) ---" | tee -a "$OUT"
  adb exec-out run-as "$PKG" sh -c 'head -n 10 "'"$CSV"'" | tr -d "\r"' 2>/dev/null | tee -a "$OUT" || true
  adb exec-out run-as "$PKG" sh -c 'tail -n 10 "'"$CSV"'" | tr -d "\r"' 2>/dev/null | tee -a "$OUT" || true
  echo "--- DEBUG: distinct dates & counts ---" | tee -a "$OUT"
  adb exec-out run-as "$PKG" sh -c '[ -f "'"$CSV"'" ] && awk -F, '"'"'NR>1{c[$1]++} END{for(k in c) printf "%s,%d\n",k,c[k]}'"'"' "'"$CSV"'" || true' 2>/dev/null | tr -d $'\r' | sort -V | tee -a "$OUT" || true
  echo "--- DEBUG: logcat (tail, filtered) ---" | tee -a "$OUT"
  adb logcat -d -v brief 2>/dev/null | grep -E "$PKG|DistanceWorker|TriggerReceiver|WorkManager|SystemJobService" | tail -n 200 | tee -a "$OUT" || true
  exit 1
}

echo "[INFO] TC2 — Distance Midnight Boundary (split by local date; no cross-day leakage)"
adb get-state >/dev/null 2>&1 || { echo "TC2 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "TC2 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

T="$(adb shell 'toybox date +%F 2>/dev/null || date +%F' | tr -d $'\r')"
Y="$(adb shell 'toybox date -d "-1 day" +%F 2>/dev/null || date -d "yesterday" +%F' 2>/dev/null | tr -d $'\r' || true)"
[ -n "$T" ] || fail "could not determine device date"

adb shell run-as "$PKG" sh <<IN >/dev/null 2>&1 || true
set -eu
mkdir -p files
printf "ts,lat,lon,accuracy\n" > "$LOC"
printf "%s 23:50:00,-33.8675,151.2070,12\n" "$Y" >> "$LOC"
printf "%s 23:55:00,-33.8676,151.2071,12\n" "$Y" >> "$LOC"
printf "%s 00:05:00,-33.8700,151.2100,12\n" "$T" >> "$LOC"
printf "%s 00:10:00,-33.8700,151.2100,12\n" "$T" >> "$LOC"
rm -f "$CSV"
IN

adb logcat -c || true
adb shell cmd activity broadcast -n "$RCV" -a "$ACT" --receiver-foreground --user 0 >/dev/null 2>&1 || true

deadline=$(( $(date +%s) + 20 ))
found=0
while [ "$(date +%s)" -lt "$deadline" ]; do
  HDR="$(adb exec-out run-as "$PKG" sed -n '1p' "$CSV" 2>/dev/null | tr -d $'\r' || true)"
  if [ "$HDR" = "date,distance_km" ]; then
    ROW_T="$(adb exec-out run-as "$PKG" awk -F, -v d="$T" 'NR>1&&$1==d{print;exit}' "$CSV" 2>/dev/null | tr -d $'\r' || true)"
    if [ -n "$ROW_T" ]; then found=1; break; fi
  fi
  sleep 1
done

[ "$found" -eq 1 ] || fail "no today row produced in $CSV"

HDR="$(adb exec-out run-as "$PKG" sed -n '1p' "$CSV" 2>/dev/null | tr -d $'\r' || true)"
[ "$HDR" = "date,distance_km" ] || fail "bad CSV header (got: ${HDR:-missing})"

VAL_T="$(adb exec-out run-as "$PKG" awk -F, -v d="$T" 'NR>1&&$1==d{print $2;exit}' "$CSV" 2>/dev/null | tr -d $'\r' || true)"
[ "$VAL_T" = "0.00" ] || fail "today distance not split correctly (expected 0.00; got ${VAL_T:-<none>})"

CT_Y="$(adb exec-out run-as "$PKG" awk -F, -v d="$Y" 'NR>1&&$1==d{c++} END{print c+0}' "$CSV" 2>/dev/null | tr -d $'\r' || echo 0)"
[ "${CT_Y:-0}" -eq 0 ] || fail "unexpected row for yesterday present (should be 0 when only today's worker runs)"

{
  echo "HEADER=$HDR"
  echo "TODAY=$T distance_km=$VAL_T"
  echo "YESTERDAY=$Y rows=$CT_Y"
  echo "--- CSV HEAD ---"
  adb exec-out run-as "$PKG" head -n 10 "$CSV" 2>/dev/null | tr -d $'\r' || true
  echo "--- CSV TAIL ---"
  adb exec-out run-as "$PKG" tail -n 10 "$CSV" 2>/dev/null | tr -d $'\r' || true
} | tee "$OUT" >/dev/null

echo "TC2 RESULT=PASS" | tee -a "$OUT"
exit 0
