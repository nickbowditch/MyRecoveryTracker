#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
CSV="files/daily_app_switching.csv"
OUT="evidence/v6.0/app_switching/tc1.1.txt"
RCV="$PKG/.TriggerReceiver"
ACT="$PKG.ACTION_RUN_APP_SWITCHING_DAILY"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "TC1 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "TC1 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

adb shell run-as "$PKG" sh <<'IN'
set -eu
f="files/daily_app_switching.csv"
mkdir -p "$(dirname "$f")"
[ -f "$f" ] || printf "date,switches,entropy\n" >"$f"
IN

T="$(adb shell toybox date +%F | tr -d '\r')"
Y="$(adb shell toybox date -d "@$(( $(adb shell toybox date +%s)-86400 ))" +%F | tr -d '\r')"
TM="$(adb shell toybox date -d "@$(( $(adb shell toybox date +%s)+86400 ))" +%F | tr -d '\r')"

deadline=$(( $(date +%s) + 30 ))
while :; do
adb shell cmd activity broadcast -n "$RCV" -a "$ACT" --receiver-foreground --user 0 >/dev/null 2>&1 || true
sleep 2
HDR="$(adb exec-out run-as "$PKG" sed -n '1p' "$CSV" 2>/dev/null | tr -d '\r' || true)"
[ "$HDR" = "date,switches,entropy" ] && break
[ "$(date +%s)" -ge "$deadline" ] && break
done

HDR="$(adb exec-out run-as "$PKG" sed -n '1p' "$CSV" 2>/dev/null | tr -d '\r' || true)"
[ "$HDR" = "date,switches,entropy" ] || { echo "TC1 RESULT=FAIL (bad header: ${HDR:-missing})" | tee "$OUT"; exit 5; }

COUNTS="$(adb exec-out run-as "$PKG" awk -F, '
NR==1{next}
{d=$1; gsub(/^[[:space:]]+|[[:space:]]+$/,"",d); if(d!="") c[d]++}
END{for(k in c) print k","c[k]}
' "$CSV" 2>/dev/null | tr -d '\r' || true)"

cT=0; cY=0; cTM=0; other=0; dups=0
if [ -n "$COUNTS" ]; then
while IFS=, read -r d n; do
[ -z "$d" ] && continue
case "$d" in
"$T")  cT="$n" ;;
"$Y")  cY="$n" ;;
"$TM") cTM="$n" ;;
*)     other=$((other+1)) ;;
esac
[ "${n:-0}" -gt 1 ] && dups=$((dups+ (n-1) ))
done <<EOF2
$COUNTS
EOF2
fi

{
echo "HEADER=$HDR"
echo "TODAY=$T count=$cT"
echo "YESTERDAY=$Y count=$cY"
echo "TOMORROW=$TM count=$cTM"
echo "other_dates=$other dup_rows_over_1=$dups"
} | tee "$OUT" >/dev/null

if [ "$dups" -eq 0 ] && [ "$other" -eq 0 ] && { [ "$cT" -ge 0 ] && [ "$cY" -ge 0 ] && [ "$cTM" -ge 0 ]; } && { [ "$cT" -gt 0 ] || [ "$cY" -gt 0 ]; }; then
echo "TC1 RESULT=PASS" | tee -a "$OUT"
exit 0
fi

echo "TC1 RESULT=FAIL" | tee -a "$OUT"
exit 1
