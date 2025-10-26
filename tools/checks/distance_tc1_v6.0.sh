#!/bin/bash
set -euo pipefail

PKG="com.nick.myrecoverytracker"
CSV="files/daily_distance_log.csv"
OUT="evidence/v6.0/distance/tc1.txt"
mkdir -p "$(dirname "$OUT")"

fail() {
echo "TC1 RESULT=FAIL ($1)" | tee "$OUT"
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
echo "--- DEBUG: distinct dates & counts ---" | tee -a "$OUT"
adb exec-out run-as "$PKG" sh -c '[ -f "'"$CSV"'" ] && awk -F, '\''NR>1 && $1~/^[0-9]{4}-[0-9]{2}-[0-9]{2}$/ {c[$1]++} END{for(k in c) print k","c[k] | "sort -V"}'\'' "'"$CSV"'" | tr -d "\r" || true' 2>/dev/null | tee -a "$OUT" || true
exit 1
}

echo "[INFO] TC1 — Distance Correct Day Boundary (one row per local day; tz tolerance ±1)"
adb get-state >/dev/null 2>&1 || { echo "TC1 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "TC1 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

T="$(adb shell 'toybox date +%F 2>/dev/null || date +%F' | tr -d $'\r')"
Y="$(adb shell 'toybox date -d "-1 day" +%F 2>/dev/null || date -d "yesterday" +%F' 2>/dev/null | tr -d $'\r' || true)"
TM="$(adb shell 'toybox date -d "+1 day" +%F 2>/dev/null || date -d "tomorrow" +%F' 2>/dev/null | tr -d $'\r' || true)"
[ -n "$T" ] || fail "could not determine device date"

HDR="$(adb exec-out run-as "$PKG" sed -n '1p' "$CSV" 2>/dev/null | tr -d $'\r' || true)"
[ "$HDR" = "date,distance_km" ] || fail "CSV missing or bad header (want: date,distance_km; got: ${HDR:-missing})"

COUNTS="$(adb exec-out run-as "$PKG" awk -F, '
NR==1{next}
$1~/^[0-9]{4}-[0-9]{2}-[0-9]{2}$/ {c[$1]++}
END{for(k in c) print k","c[k]}
' "$CSV" 2>/dev/null | tr -d $'\r' || true)"

dups=0; other=0; cT=0; cY=0; cTM=0
if [ -n "$COUNTS" ]; then
while IFS=, read -r d n; do
[ -z "$d" ] && continue
case "$d" in
"$T")  cT="$n" ;;
"$Y")  cY="$n" ;;
"$TM") cTM="$n" ;;
*)     other=$((other+1)) ;;
esac
[ "${n:-0}" -gt 1 ] && dups=$((dups + (n-1) ))
done <<EOF2
$COUNTS
EOF2
fi

LAST_DATE="$(adb exec-out run-as "$PKG" awk -F, 'NR>1 && $1~/^[0-9]{4}-[0-9]{2}-[0-9]{2}$/ {ld=$1} END{if(ld!="") print ld}' "$CSV" 2>/dev/null | tr -d $'\r' || true)"
[ -n "$LAST_DATE" ] || fail "no data rows present in CSV"
if [ "$LAST_DATE" != "$T" ] && [ "$LAST_DATE" != "$Y" ] && [ "$LAST_DATE" != "$TM" ]; then
fail "last row date $LAST_DATE outside tolerance (not today/yesterday/tomorrow)"
fi
[ "$dups" -eq 0 ] || fail "duplicate date rows present"

{
echo "HEADER=$HDR"
echo "TODAY=$T count=$cT"
echo "YESTERDAY=$Y count=$cY"
echo "TOMORROW=$TM count=$cTM"
echo "other_dates=$other dup_rows_over_1=$dups"
echo "LAST_DATE=$LAST_DATE"
} | tee "$OUT" >/dev/null

echo "TC1 RESULT=PASS" | tee -a "$OUT"
exit 0
