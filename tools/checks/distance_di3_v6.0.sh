#!/bin/bash
set -euo pipefail

PKG="com.nick.myrecoverytracker"
CSV="files/daily_distance_log.csv"
OUT="evidence/v6.0/distance/di3.txt"
mkdir -p "$(dirname "$OUT")"

fail() {
  echo "DI3 RESULT=FAIL ($1)" | tee "$OUT"
  echo "--- DEBUG: device date/time (local) ---" | tee -a "$OUT"
  (adb shell 'toybox date "+%F %T %Z %z" 2>/dev/null || date "+%F %T %Z %z"') | tr -d $'\r' | tee -a "$OUT"
  echo "--- DEBUG: app files dir ---" | tee -a "$OUT"
  adb exec-out run-as "$PKG" sh -c 'pwd; ls -la files' 2>/dev/null | tr -d $'\r' | tee -a "$OUT" || true
  echo "--- DEBUG: CSV head ---" | tee -a "$OUT"
  adb exec-out run-as "$PKG" sh -c 'head -n 20 "'"$CSV"'" | tr -d "\r"' 2>/dev/null | tee -a "$OUT" || true
  echo "--- DEBUG: CSV tail ---" | tee -a "$OUT"
  adb exec-out run-as "$PKG" sh -c 'tail -n 20 "'"$CSV"'" | tr -d "\r"' 2>/dev/null | tee -a "$OUT" || true
  exit 1
}

adb get-state >/dev/null 2>&1 || { echo "DI3 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "DI3 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }
adb exec-out run-as "$PKG" sh -c '[ -f "'"$CSV"'" ]' >/dev/null 2>&1 || fail "CSV not found at $CSV"

HDR="$(adb exec-out run-as "$PKG" sed -n '1p' "$CSV" 2>/dev/null | tr -d $'\r' || true)"
[ -n "$HDR" ] || fail "empty CSV header"
[ "$HDR" = "date,distance_km" ] || fail "unsupported header '$HDR' (want: date,distance_km)"

TOTAL_ROWS="$(adb exec-out run-as "$PKG" awk -F, 'NR>1{rows++} END{print rows+0}' "$CSV" 2>/dev/null | tr -d $'\r' || echo 0)"

INVALIDS="$(adb exec-out run-as "$PKG" awk -F, '
NR==1{next}
{
  d=$1; k=$2; err=""
  gsub(/^[[:space:]]+|[[:space:]]+$/,"",d); gsub(/^[[:space:]]+|[[:space:]]+$/,"",k)
  if (d !~ /^[0-9]{4}-[0-9]{2}-[0-9]{2}$/) err=err "bad_date;"
  # accept integers or decimals
  if (k !~ /^([0-9]+)(\.[0-9]+)?$/) err=err "km_not_number;"
  else {
    # numeric compare against 0..200.00
    split(k, p, "."); i=p[1]+0; f=(length(p)>1?("0." p[2])+0:0.0)
    val=i+f
    if (val<0 || val>200.0) err=err "km_oob;"
  }
  if (NF!=2) err=err "wrong_cols;"
  if (err!="") print NR ":" $0 " => " err
}' "$CSV" 2>/dev/null | tr -d $'\r' || true)"

BAD_COUNT="$(printf "%s\n" "$INVALIDS" | sed '/^$/d' | wc -l | tr -d ' ')"

{
  echo "CSV=$CSV"
  echo "HEADER=$HDR"
  echo "ROWS_TOTAL=$TOTAL_ROWS"
  echo "INVALID_COUNT=$BAD_COUNT"
  echo "--- INVALID SAMPLES (max 50) ---"
  printf "%s\n" "$INVALIDS" | sed -n '1,50p'
} | tee "$OUT" >/dev/null

[ "${BAD_COUNT:-0}" -eq 0 ] || { echo "DI3 RESULT=FAIL (types/bounds violations present)" | tee -a "$OUT"; exit 1; }

echo "DI3 RESULT=PASS" | tee -a "$OUT"
exit 0
