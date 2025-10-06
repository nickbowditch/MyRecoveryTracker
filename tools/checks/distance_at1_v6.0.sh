#!/bin/bash
set -euo pipefail

PKG="com.nick.myrecoverytracker"
CSV="files/daily_distance_log.csv"
LOC="files/location_log.csv"
RCV="$PKG/.TriggerReceiver"
ACT="$PKG.ACTION_RUN_DISTANCE_DAILY"
OUT="evidence/v6.0/distance/at1.txt"
mkdir -p "$(dirname "$OUT")"

fail() {
  echo "AT1 RESULT=FAIL ($1)" | tee "$OUT"
  echo "--- DEBUG: device date/time ---" | tee -a "$OUT"
  (adb shell 'toybox date "+%F %T %Z %z" 2>/dev/null || date "+%F %T %Z %z"') | tr -d $'\r' | tee -a "$OUT"
  echo "--- DEBUG: before/after hashes ---" | tee -a "$OUT"
  echo "before_sha=${BEFORE_SHA:-<none>} after_sha=${AFTER_SHA:-<none>}" | tee -a "$OUT"
  echo "--- DEBUG: today row before/after ---" | tee -a "$OUT"
  echo "row_before=${ROW_BEFORE:-<none>}" | tee -a "$OUT"
  echo "row_after=${ROW_AFTER:-<none>}" | tee -a "$OUT"
  echo "--- DEBUG: CSV head/tail ---" | tee -a "$OUT"
  adb exec-out run-as "$PKG" sh -c 'head -n 20 "'"$CSV"'" 2>/dev/null | tr -d "\r"' | tee -a "$OUT" || true
  adb exec-out run-as "$PKG" sh -c 'tail -n 20 "'"$CSV"'" 2>/dev/null | tr -d "\r"' | tee -a "$OUT" || true
  echo "--- DEBUG: location_log.csv head ---" | tee -a "$OUT"
  adb exec-out run-as "$PKG" sh -c 'head -n 10 "'"$LOC"'" 2>/dev/null | tr -d "\r"' | tee -a "$OUT" || true
  echo "--- DEBUG: logcat tail (filtered) ---" | tee -a "$OUT"
  adb logcat -d -v brief 2>/dev/null | grep -E "$PKG|DistanceWorker|TriggerReceiver|WorkManager|SystemJobService" | tail -n 200 | tee -a "$OUT" || true
  exit 1
}

adb get-state >/dev/null 2>&1 || { echo "AT1 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "AT1 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

T="$(adb shell 'toybox date +%F 2>/dev/null || date +%F' | tr -d $'\r')"
[ -n "$T" ] || fail "could not determine device date"

adb shell run-as "$PKG" sh <<'IN'
set -eu
LOC="files/location_log.csv"
CSV="files/daily_distance_log.csv"
mkdir -p files
printf "ts,lat,lon,accuracy\n" >"$LOC"
printf "%s 08:00:00,-33.8675,151.2070,12\n" "$(toybox date +%F)" >>"$LOC"
printf "%s 08:30:00,-33.8700,151.2100,10\n" "$(toybox date +%F)" >>"$LOC"
if [ -f "$CSV" ]; then
  hdr="$(sed -n '1p' "$CSV" || true)"
  if [ "$hdr" != "date,distance_km" ]; then
    printf "date,distance_km\n" >"$CSV"
  fi
else
  printf "date,distance_km\n" >"$CSV"
fi
tmp="$(awk -F, -v d="$(toybox date +%F)" 'NR==1{print;found=0;next} {if($1==d){found=1}else print} END{if(!found) print d",0.00"}' "$CSV")"
printf "%s\n" "$tmp" >"$CSV"
IN

ROW_BEFORE="$(adb exec-out run-as "$PKG" awk -F, -v d="$T" 'NR>1&&$1==d{print;exit}' "$CSV" 2>/dev/null | tr -d $'\r' || true)"
BEFORE_SHA="$(adb exec-out run-as "$PKG" cat "$CSV" 2>/dev/null | shasum -a 256 2>/dev/null | awk '{print $1}' || echo "")"

adb logcat -c || true
adb shell cmd activity broadcast -n "$RCV" -a "$ACT" --receiver-foreground --user 0 >/dev/null 2>&1 || true

deadline=$(( $(date +%s) + 20 ))
updated=0
while [ "$(date +%s)" -lt "$deadline" ]; do
  ROW_AFTER="$(adb exec-out run-as "$PKG" awk -F, -v d="$T" 'NR>1&&$1==d{print;exit}' "$CSV" 2>/dev/null | tr -d $'\r' || true)"
  AFTER_SHA="$(adb exec-out run-as "$PKG" cat "$CSV" 2>/dev/null | shasum -a 256 2>/dev/null | awk '{print $1}' || echo "")"
  VAL_AFTER="$(printf "%s" "${ROW_AFTER:-}" | awk -F, '{print $2}' || true)"
  if [ -n "${ROW_AFTER:-}" ] && [ "${VAL_AFTER:-0.00}" != "0.00" ] && [ -n "${AFTER_SHA:-}" ] && [ "$AFTER_SHA" != "$BEFORE_SHA" ]; then
    updated=1
    break
  fi
  sleep 1
done

[ "$updated" -eq 1 ] || fail "manual trigger did not update CSV row for $T"

{
  echo "today=$T"
  echo "before_sha=$BEFORE_SHA"
  echo "after_sha=$AFTER_SHA"
  echo "row_before=$ROW_BEFORE"
  echo "row_after=$ROW_AFTER"
} | tee "$OUT" >/dev/null

echo "AT1 RESULT=PASS" | tee -a "$OUT"
exit 0
