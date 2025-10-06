#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
CSV="files/app_category_daily.csv"
OUT="evidence/v6.0/app_usage_by_category/tc1.1.txt"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "TC1 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "TC1 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

HDR="$(adb exec-out run-as "$PKG" sed -n '1p' "$CSV" 2>/dev/null | tr -d '\r' || true)"
[ -n "$HDR" ] || { echo "TC1 RESULT=FAIL (missing csv)" | tee "$OUT"; exit 4; }
[ "$HDR" = "date,category,minutes" ] || { echo "TC1 RESULT=FAIL (bad header)" | tee "$OUT"; exit 5; }

T="$(adb shell toybox date +%F | tr -d '\r')"
Y="$(adb shell toybox date -d "@$(( $(adb shell toybox date +%s)-86400 ))" +%F | tr -d '\r')"
TM="$(adb shell toybox date -d "@$(( $(adb shell toybox date +%s)+86400 ))" +%F | tr -d '\r')"

COUNTS="$(adb exec-out run-as "$PKG" awk -F, '
NR==1{next}
{d=$1; gsub(/^[[:space:]]+|[[:space:]]+$/,"",d); if(d!="") c[d]++}
END{ for(k in c) print k","c[k] }
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

pass=1
if [ "$other" -eq 0 ]; then
  if [ "$cT" -gt 0 ] && [ "$cY" -eq 0 ] && [ "$cTM" -eq 0 ]; then pass=0; fi
  if [ "$cT" -eq 0 ] && [ "$cY" -gt 0 ] && [ "$cTM" -eq 0 ]; then pass=0; fi
  if [ "$cT" -eq 0 ] && [ "$cY" -eq 0 ] && [ "$cTM" -gt 0 ]; then pass=0; fi
fi

[ $pass -eq 0 ] && echo "TC1 RESULT=PASS" | tee "$OUT" || echo "TC1 RESULT=FAIL (counts: Y=$cY T=$cT TM=$cTM other_dates=$other)" | tee "$OUT"
exit $pass
