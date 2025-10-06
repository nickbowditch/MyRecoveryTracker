#!/bin/sh
set -eu

PKG="com.nick.myrecoverytracker"
LOC="files/location_log.csv"
DAY="files/daily_distance_log.csv"
OUT="evidence/v6.0/distance/schema.txt"
mkdir -p "$(dirname "$OUT")"

fail() {
  echo "[FAIL] $1"
  echo "[DEBUG] device time"
  adb shell date '+%F %T %Z %z' 2>/dev/null | tr -d '\r' || true
  echo "[DEBUG] app dir"
  adb exec-out run-as "$PKG" sh -c 'pwd; ls -la' 2>/dev/null || true
  echo "[DEBUG] location_log.csv head/tail"
  adb exec-out run-as "$PKG" sh -c 'head -n 5 "'"$LOC"'" 2>/dev/null | tr -d "\r" || echo "<missing>"'
  adb exec-out run-as "$PKG" sh -c 'tail -n 5 "'"$LOC"'" 2>/dev/null | tr -d "\r" || echo "<missing>"'
  echo "[DEBUG] daily_distance_log.csv head/tail"
  adb exec-out run-as "$PKG" sh -c 'head -n 5 "'"$DAY"'" 2>/dev/null | tr -d "\r" || echo "<missing>"'
  adb exec-out run-as "$PKG" sh -c 'tail -n 5 "'"$DAY"'" 2>/dev/null | tr -d "\r" || echo "<missing>"'
  echo "RESULT=FAIL"
  exit 1
}

echo "[INFO] Distance Schema Drift Check"
adb get-state | grep -q "^device$" || { echo "SCHEMA RESULT=FAIL (no device)"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "SCHEMA RESULT=FAIL (app not installed)"; exit 3; }

LOC_HDR="$(adb exec-out run-as "$PKG" sed -n '1p' "$LOC" 2>/dev/null | tr -d $'\r' || true)"
DAY_HDR="$(adb exec-out run-as "$PKG" sed -n '1p' "$DAY" 2>/dev/null | tr -d $'\r' || true)"

echo "EXPECTED(location_log.csv)=ts,lat,lon,accuracy" | tee "$OUT" >/dev/null
echo "EXPECTED(daily_distance_log.csv)=date,distance_km" | tee -a "$OUT" >/dev/null
echo "ACTUAL(location_log.csv)=${LOC_HDR:-<missing>}" | tee -a "$OUT" >/dev/null
echo "ACTUAL(daily_distance_log.csv)=${DAY_HDR:-<missing>}" | tee -a "$OUT" >/dev/null

[ "$LOC_HDR" = "ts,lat,lon,accuracy" ] || fail "location_log.csv header mismatch (got: ${LOC_HDR:-missing})"
[ "$DAY_HDR" = "date,distance_km" ] || fail "daily_distance_log.csv header mismatch (got: ${DAY_HDR:-missing})"

BAD_LOC_ROWS="$(adb exec-out run-as "$PKG" awk -F, 'NR>1{if(NF<4||NF>4) {print; c++} if(++n==10) exit} END{exit c?0:1}' "$LOC" 2>/dev/null | tr -d $'\r' || true)"
BAD_DAY_ROWS="$(adb exec-out run-as "$PKG" awk -F, 'NR>1{if(NF<2||NF>2) {print; c++} if(++n==10) exit} END{exit c?0:1}' "$DAY" 2>/dev/null | tr -d $'\r' || true)"

if [ -n "$BAD_LOC_ROWS" ]; then
  echo "[WARN] location_log.csv has rows with unexpected column count (sample below)" | tee -a "$OUT" >/dev/null
  printf "%s\n" "$BAD_LOC_rows" | tee -a "$OUT" >/dev/null || true
fi
if [ -n "$BAD_DAY_ROWS" ]; then
  echo "[WARN] daily_distance_log.csv has rows with unexpected column count (sample below)" | tee -a "$OUT" >/dev/null
  printf "%s\n" "$BAD_DAY_ROWS" | tee -a "$OUT" >/dev/null || true
fi

echo "[PASS] Headers match golden schema."
echo "RESULT=PASS" | tee -a "$OUT"
exit 0
