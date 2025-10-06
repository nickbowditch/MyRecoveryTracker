#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
CSV="files/daily_app_switching.csv"
OUT="evidence/v6.0/app_switching/tc2.1.txt"
RCV="$PKG/.TriggerReceiver"
ACT="$PKG.ACTION_RUN_APP_SWITCHING_DAILY"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "TC2 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "TC2 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

T="$(adb shell toybox date +%F | tr -d '\r')"
Y="$(adb shell toybox date -d "@$(( $(adb shell toybox date +%s)-86400 ))" +%F | tr -d '\r')"
TM="$(adb shell toybox date -d "@$(( $(adb shell toybox date +%s)+86400 ))" +%F | tr -d '\r')"

adb shell run-as "$PKG" sh <<IN
set -eu
f="files/daily_app_switching.csv"
mkdir -p "\$(dirname "\$f")"
printf "date,switches,entropy\n" >"\$f"
printf "%s,5,1.2\n" "$Y" >>"\$f"
printf "%s,10,2.3\n" "$T" >>"\$f"
IN

deadline=$(( $(date +%s) + 30 ))
while :; do
adb shell cmd activity broadcast -n "$RCV" -a "$ACT" --receiver-foreground --user 0 >/dev/null 2>&1 || true
sleep 2

COUNTS="$(adb exec-out run-as "$PKG" awk -F, '
NR==1{next}
{d=$1; gsub(/^[[:space:]]+|[[:space:]]+$/,"",d); if(d!="") c[d]++}
END{for(k in c) print k","c[k]}
' "$CSV" 2>/dev/null | tr -d '\r' || true)"

cT=0; cY=0; cTM=0; other=0
if [ -n "$COUNTS" ]; then
while IFS=, read -r d n; do
[ -z "$d" ] && continue
if   [ "$d" = "$T" ];  then cT="$n"
elif [ "$d" = "$Y" ];  then cY="$n"
elif [ "$d" = "$TM" ]; then cTM="$n"
else other=$((other+1))
fi
done <<EOF2
$COUNTS
EOF2
fi

if [ "$cY" -gt 0 ] && [ "$cTM" -eq 0 ] && [ "$other" -eq 0 ]; then
{
echo "TC2 RESULT=PASS"
echo "counts: Y=$cY T=$cT TM=$cTM other_dates=$other"
} | tee "$OUT"
exit 0
fi

[ "$(date +%s)" -ge "$deadline" ] && break
done

{
echo "TC2 RESULT=FAIL (counts: Y=$cY T=$cT TM=$cTM other_dates=$other)"
echo "--- DEBUG ---"
adb exec-out run-as "$PKG" head -n10 "$CSV" 2>/dev/null || echo "[no file]"
} | tee "$OUT"
exit 1
