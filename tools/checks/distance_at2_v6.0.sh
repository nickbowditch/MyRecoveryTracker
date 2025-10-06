#!/bin/bash
set -euo pipefail

PKG="com.nick.myrecoverytracker"
RAW_METERS="files/distance_log.csv"
RAW_LOC="files/location_log.csv"
DAILY="files/daily_distance_log.csv"
OUT="evidence/v6.0/distance/at2.txt"
mkdir -p "$(dirname "$OUT")"

fail() {
  echo "AT2 RESULT=FAIL ($1)" | tee "$OUT"
  echo "--- DEBUG: device date/time ---" | tee -a "$OUT"
  (adb shell 'toybox date "+%F %T %Z %z" 2>/dev/null || date "+%F %T %Z %z"') | tr -d $'\r' | tee -a "$OUT"
  echo "--- DEBUG: files present ---" | tee -a "$OUT"
  adb exec-out run-as "$PKG" sh -c 'pwd; ls -la files' 2>/dev/null | tr -d $'\r' | tee -a "$OUT" || true
  echo "--- DEBUG: headers ---" | tee -a "$OUT"
  [ -n "${RAW_PATH:-}" ] && { echo "[RAW:$RAW_PATH]" | tee -a "$OUT"; adb exec-out run-as "$PKG" sh -c 'sed -n "1p" "'"$RAW_PATH"'"' 2>/dev/null | tr -d $'\r' | tee -a "$OUT" || true; } || true
  echo "[DAILY:$DAILY]" | tee -a "$OUT"
  adb exec-out run-as "$PKG" sh -c 'sed -n "1p" "'"$DAILY"'"' 2>/dev/null | tr -d $'\r' | tee -a "$OUT" || true
  echo "--- DEBUG: RAW head/tail ---" | tee -a "$OUT"
  [ -n "${RAW_PATH:-}" ] && { adb exec-out run-as "$PKG" sh -c 'head -n 10 "'"$RAW_PATH"'"' 2>/dev/null | tr -d $'\r' | tee -a "$OUT" || true; adb exec-out run-as "$PKG" sh -c 'tail -n 10 "'"$RAW_PATH"'"' 2>/dev/null | tr -d $'\r' | tee -a "$OUT" || true; } || true
  echo "--- DEBUG: DAILY head/tail ---" | tee -a "$OUT"
  adb exec-out run-as "$PKG" sh -c 'head -n 10 "'"$DAILY"'"' 2>/dev/null | tr -d $'\r' | tee -a "$OUT" || true
  adb exec-out run-as "$PKG" sh -c 'tail -n 10 "'"$DAILY"'"' 2>/dev/null | tr -d $'\r' | tee -a "$OUT" || true
  echo "--- DEBUG: logcat tail (filtered) ---" | tee -a "$OUT"
  adb logcat -d -v brief 2>/dev/null | grep -E "$PKG|DistanceWorker|TriggerReceiver|WorkManager|SystemJobService" | tail -n 200 | tee -a "$OUT" || true
  exit 1
}

adb get-state >/dev/null 2>&1 || { echo "AT2 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "AT2 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

RAW_PATH=""
if adb exec-out run-as "$PKG" sh -c '[ -f "'"$RAW_LOC"'" ] && echo yes || echo no' 2>/dev/null | grep -q '^yes$'; then
  RAW_PATH="$RAW_LOC"
elif adb exec-out run-as "$PKG" sh -c '[ -f "'"$RAW_METERS"'" ] && echo yes || echo no' 2>/dev/null | grep -q '^yes$'; then
  RAW_PATH="$RAW_METERS"
fi
[ -n "$RAW_PATH" ] || fail "no raw distance file found (checked: $RAW_LOC, $RAW_METERS)"

DAILY_HDR="$(adb exec-out run-as "$PKG" sed -n '1p' "$DAILY" 2>/dev/null | tr -d $'\r' || true)"
[ "$DAILY_HDR" = "date,distance_km" ] || fail "daily CSV missing or bad header (want: date,distance_km; got: ${DAILY_HDR:-missing})"

RAW_HDR="$(adb exec-out run-as "$PKG" sed -n '1p' "$RAW_PATH" 2>/dev/null | tr -d $'\r' || true)"
RAW_MODE=""
case "$RAW_HDR" in
  "ts,meters") RAW_MODE="METERS" ;;
  "ts,lat,lon,accuracy") RAW_MODE="LOC" ;;
  *) fail "unsupported raw header '$RAW_HDR' (want: ts,meters | ts,lat,lon,accuracy)" ;;
esac

if [ "$RAW_MODE" = "METERS" ]; then
  RECOMP="$(adb exec-out run-as "$PKG" sh -c 'tail -n +2 "'"$RAW_PATH"'"' 2>/dev/null | tr -d $'\r' | awk -F, '
    { d=substr($1,1,10); m=$2+0; if(d~/^[0-9]{4}-[0-9]{2}-[0-9]{2}$/) sum[d]+=m; }
    END{ for(d in sum) printf "%s,%.6f\n", d, sum[d]/1000.0 }
  ' || true)"
else
  RECOMP="$(adb exec-out run-as "$PKG" sh -c 'tail -n +2 "'"$RAW_PATH"'"' 2>/dev/null | tr -d $'\r' | awk -F, '
    function rad(x){return x*3.141592653589793/180.0}
    function hav(lat1,lon1,lat2,lon2,  dlat,dlon,a,c,r){
      r=6371.0
      dlat=rad(lat2-lat1); dlon=rad(lon2-lon1)
      a=sin(dlat/2)*sin(dlat/2)+cos(rad(lat1))*cos(rad(lat2))*sin(dlon/2)*sin(dlon/2)
      c=2*atan2(sqrt(a),sqrt(1-a))
      return r*c
    }
    {
      ts=$1; lat=$2+0; lon=$3+0
      d=substr(ts,1,10)
      if(d~/^[0-9]{4}-[0-9]{2}-[0-9]{2}$/){
        if(seen[d]){
          km[d]+=hav(prev_lat[d],prev_lon[d],lat,lon)
        } else {
          seen[d]=1
        }
        prev_lat[d]=lat; prev_lon[d]=lon
      }
    }
    END{ for(d in km) printf "%s,%.6f\n", d, km[d] }
  ' || true)"
fi

[ -n "${RECOMP// /}" ] || fail "no recomputed distances from RAW_MODE=$RAW_MODE"

DAILY_KM="$(adb exec-out run-as "$PKG" awk -F, 'NR>1 && $1~/^[0-9]{4}-[0-9]{2}-[0-9]{2}$/ {printf "%s,%.6f\n",$1,$2+0}' "$DAILY" 2>/dev/null | tr -d $'\r' || true)"
[ -n "${DAILY_KM// /}" ] || fail "no rows in daily CSV"

SUMMARY_LINE="$(awk -F, -v OFS=, -v tol_pct=2.0 '
  BEGIN{ while((getline < ARGV[1])>0){ gsub(/\r/,""); if($0=="") continue; R[$1]=$2+0; }
         ARGV[1]="" }
  { D[$1]=$2+0 }
  END{
    days=0; mism=0; miss_raw=0; miss_daily=0;
    for (d in D) {
      days++;
      if (!(d in R)) { print "MISS_RAW",d,D[d],0; miss_raw++; next }
      a=D[d]; b=R[d];
      base=(b!=0?b:(a!=0?a:1.0))
      diff=(a-b); if(diff<0) diff=-diff
      pct=100.0*diff/base
      if (pct>tol_pct) { print "MISMATCH",d,a,b,pct; mism++; } else { print "OK",d,a,b,pct; }
      seen[d]=1
    }
    for (d in R) if (!(d in seen)) { print "MISS_DAILY",d,0,R[d]; miss_daily++; }
    printf "SUMMARY,%d,%d,%d,%d\n", days, mism, miss_raw, miss_daily
  }
' <(printf "%s\n" "$RECOMP") <(printf "%s\n" "$DAILY_KM") | tee >(sed -n '1,50p' > /tmp/_at2_samples.$$) | tail -n 1)"

DAYS="$(printf "%s" "$SUMMARY_LINE" | awk -F, '/^SUMMARY/{print $2}')"
MISM="$(printf "%s" "$SUMMARY_LINE" | awk -F, '/^SUMMARY/{print $3}')"
MISS_RAW="$(printf "%s" "$SUMMARY_LINE" | awk -F, '/^SUMMARY/{print $4}')"
MISS_DAILY="$(printf "%s" "$SUMMARY_LINE" | awk -F, '/^SUMMARY/{print $5}')"

{
  echo "raw_path=$RAW_PATH"
  echo "raw_mode=$RAW_MODE"
  echo "daily_path=$DAILY"
  echo "days_compared=${DAYS:-0} mismatches=${MISM:-0} missing_in_raw=${MISS_RAW:-0} missing_in_daily=${MISS_DAILY:-0}"
  echo "--- SAMPLE COMPARISONS (first 50) ---"
  cat /tmp/_at2_samples.$$ 2>/dev/null || true
} | tee "$OUT" >/dev/null
rm -f /tmp/_at2_samples.$$ 2>/dev/null || true

if [ "${MISM:-0}" -eq 0 ] && [ "${MISS_RAW:-0}" -eq 0 ]; then
  echo "AT2 RESULT=PASS" | tee -a "$OUT"
  exit 0
fi

echo "AT2 RESULT=FAIL" | tee -a "$OUT"
exit 1
