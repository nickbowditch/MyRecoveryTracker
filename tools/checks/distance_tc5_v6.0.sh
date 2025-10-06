#!/bin/bash
set -euo pipefail

PKG="com.nick.myrecoverytracker"
CSV="files/daily_distance_log.csv"
OUT="evidence/v6.0/distance/tc5.txt"
mkdir -p "$(dirname "$OUT")"

fail() {
  echo "TC5 RESULT=FAIL ($1)" | tee "$OUT"
  echo "--- DEBUG: device date/time (local) ---" | tee -a "$OUT"
  (adb shell 'toybox date "+%F %T %Z %z" 2>/dev/null || date "+%F %T %Z %z"') | tr -d $'\r' | tee -a "$OUT" || true
  echo "--- DEBUG: app files dir (pwd; ls -la) ---" | tee -a "$OUT"
  adb exec-out run-as "$PKG" sh -c 'pwd; ls -la' 2>/dev/null | tr -d $'\r' | tee -a "$OUT" || true
  echo "--- DEBUG: CSV exists/size ---" | tee -a "$OUT"
  adb exec-out run-as "$PKG" sh -c '[ -f "'"$CSV"'" ] && { ls -l "'"$CSV"'"; wc -l "'"$CSV"'"; } || echo "<missing>"' 2>/dev/null | tr -d $'\r' | tee -a "$OUT" || true
  echo "--- DEBUG: CSV head ---" | tee -a "$OUT"
  adb exec-out run-as "$PKG" sh -c 'head -n 20 "'"$CSV"'" | tr -d "\r"' 2>/dev/null | tee -a "$OUT" || true
  echo "--- DEBUG: CSV tail ---" | tee -a "$OUT"
  adb exec-out run-as "$PKG" sh -c 'tail -n 20 "'"$CSV"'" | tr -d "\r"' 2>/dev/null | tee -a "$OUT" || true
  if [ -n "${GAPS:-}" ]; then
    echo "--- DEBUG: gaps over 2 days (prev_date -> curr_date : gap_days) ---" | tee -a "$OUT"
    printf "%s\n" "$GAPS" | tee -a "$OUT"
  fi
  exit 1
}

echo "[INFO] TC5 — Distance No >2-day gaps"
adb get-state >/dev/null 2>&1 || { echo "TC5 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "TC5 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

HDR="$(adb exec-out run-as "$PKG" sed -n '1p' "$CSV" 2>/dev/null | tr -d $'\r' || true)"
[ "$HDR" = "date,distance_km" ] || fail "CSV missing or bad header (want: date,distance_km; got: ${HDR:-missing})"

DATES="$(adb exec-out run-as "$PKG" awk -F, 'NR>1 && $1~/^[0-9]{4}-[0-9]{2}-[0-9]{2}$/ {print $1}' "$CSV" 2>/dev/null | tr -d $'\r' | sort -u || true)"
[ -n "${DATES// /}" ] || { echo "dates=0 (nothing to check)"; echo "TC5 RESULT=PASS" | tee "$OUT"; exit 0; }

COUNT_DATES="$(printf "%s\n" "$DATES" | awk 'NF{c++} END{print c+0}')"

prev=""
prev_epoch=""
GAPS=""
while read -r d; do
  [ -z "$d" ] && continue
  e="$(adb shell toybox date -d "$d" +%s 2>/dev/null | tr -d $'\r' || true)"
  if [ -z "$e" ]; then
    fail "could not convert date '$d' to epoch on device"
  fi
  if [ -n "$prev" ]; then
    diff_days=$(( (e - prev_epoch) / 86400 ))
    if [ "$diff_days" -gt 2 ]; then
      GAPS="${GAPS}${prev} -> ${d} : ${diff_days}d
"
    fi
  fi
  prev="$d"
  prev_epoch="$e"
done <<EOF2
$DATES
EOF2

if [ -n "${GAPS// /}" ]; then
  fail "found gap(s) > 2 days between consecutive dates"
fi

{
  echo "HEADER=$HDR"
  echo "distinct_dates=$COUNT_DATES"
  echo "--- first 10 dates ---"
  printf "%s\n" "$DATES" | head -n 10
  echo "--- last 10 dates ---"
  printf "%s\n" "$DATES" | tail -n 10
  echo "--- CSV HEAD ---"
  adb exec-out run-as "$PKG" head -n 10 "$CSV" 2>/dev/null | tr -d $'\r' || true
  echo "--- CSV TAIL ---"
  adb exec-out run-as "$PKG" tail -n 10 "$CSV" 2>/dev/null | tr -d $'\r' || true
} | tee "$OUT" >/dev/null

echo "TC5 RESULT=PASS" | tee -a "$OUT"
exit 0
