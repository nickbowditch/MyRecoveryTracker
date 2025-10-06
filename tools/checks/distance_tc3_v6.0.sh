#!/bin/bash
set -euo pipefail

PKG="com.nick.myrecoverytracker"
CSV="files/daily_distance_log.csv"
OUT="evidence/v6.0/distance/tc3.txt"
mkdir -p "$(dirname "$OUT")"

fail() {
  echo "TC3 RESULT=FAIL ($1)" | tee "$OUT"
  echo "--- DEBUG: device date/time (local) ---" | tee -a "$OUT"
  (adb shell 'toybox date "+%F %T %Z %z" 2>/dev/null || date "+%F %T %Z %z"') | tr -d $'\r' | tee -a "$OUT" || true
  echo "--- DEBUG: app files dir (pwd; ls -la) ---" | tee -a "$OUT"
  adb exec-out run-as "$PKG" sh -c 'pwd; ls -la' 2>/dev/null | tr -d $'\r' | tee -a "$OUT" || true
  echo "--- DEBUG: CSV exists/size ---" | tee -a "$OUT"
  adb exec-out run-as "$PKG" sh -c '[ -f "'"$CSV"'" ] && { ls -l "'"$CSV"'"; wc -l "'"$CSV"'"; } || echo "<missing>"' 2>/dev/null | tr -d $'\r' | tee -a "$OUT" || true
  echo "--- DEBUG: daily_distance_log.csv (head) ---" | tee -a "$OUT"
  adb exec-out run-as "$PKG" sh -c 'head -n 20 "'"$CSV"'" | tr -d "\r"' 2>/dev/null | tee -a "$OUT" || true
  echo "--- DEBUG: daily_distance_log.csv (tail) ---" | tee -a "$OUT"
  adb exec-out run-as "$PKG" sh -c 'tail -n 20 "'"$CSV"'" | tr -d "\r"' 2>/dev/null | tee -a "$OUT" || true
  if [ -n "${FUTURE_SAMPLE:-}" ]; then
    echo "--- DEBUG: future rows (sample) ---" | tee -a "$OUT"
    printf "%s\n" "$FUTURE_SAMPLE" | tee -a "$OUT"
  fi
  exit 1
}

echo "[INFO] TC3 — Distance No Future Dates"
adb get-state >/dev/null 2>&1 || { echo "TC3 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "TC3 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

TODAY="$(adb shell 'toybox date +%F 2>/dev/null || date +%F' | tr -d $'\r')"
[ -n "$TODAY" ] || fail "could not determine device date"

HDR="$(adb exec-out run-as "$PKG" sed -n '1p' "$CSV" 2>/dev/null | tr -d $'\r' || true)"
[ "$HDR" = "date,distance_km" ] || fail "CSV missing or bad header (want: date,distance_km; got: ${HDR:-missing})"

FUTURE_COUNT="$(adb exec-out run-as "$PKG" awk -F, -v t="$TODAY" 'NR>1 && $1~/^[0-9]{4}-[0-9]{2}-[0-9]{2}$/ && $1>t {c++} END{print c+0}' "$CSV" 2>/dev/null | tr -d $'\r' || echo 0)"
if [ "${FUTURE_COUNT:-0}" -gt 0 ]; then
  FUTURE_SAMPLE="$(adb exec-out run-as "$PKG" awk -F, -v t="$TODAY" 'NR>1 && $1~/^[0-9]{4}-[0-9]{2}-[0-9]{2}$/ && $1>t {print; if(++n==20) exit}' "$CSV" 2>/dev/null | tr -d $'\r' || true)"
  fail "found ${FUTURE_COUNT} future-dated row(s) beyond $TODAY"
fi

{
  echo "HEADER=$HDR"
  echo "TODAY=$TODAY"
  echo "future_rows=0"
  echo "--- CSV HEAD ---"
  adb exec-out run-as "$PKG" head -n 10 "$CSV" 2>/dev/null | tr -d $'\r' || true
  echo "--- CSV TAIL ---"
  adb exec-out run-as "$PKG" tail -n 10 "$CSV" 2>/dev/null | tr -d $'\r' || true
} | tee "$OUT" >/dev/null

echo "TC3 RESULT=PASS" | tee -a "$OUT"
exit 0
