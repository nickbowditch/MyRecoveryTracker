#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/metrics/di1.1.txt"
CSV="files/daily_metrics.csv"
LOCK="app/locks/daily_metrics.header"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "DI-1 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "DI-1 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

EXP="$(tr -d '\r' < "$LOCK" 2>/dev/null || true)"
[ -n "$EXP" ] || { echo "DI-1 RESULT=FAIL (missing lock)" | tee "$OUT"; exit 4; }

HDR="$(adb exec-out run-as "$PKG" head -n1 "$CSV" 2>/dev/null | tr -d '\r' || true)"
[ -n "$HDR" ] || { echo "DI-1 RESULT=FAIL (missing csv)" | tee "$OUT"; exit 5; }
[ "$HDR" = "$EXP" ] || { echo "DI-1 RESULT=FAIL (header drift)" | tee "$OUT"; exit 6; }

TODAY="$(adb shell toybox date +%F 2>/dev/null | tr -d '\r')"
YDAY="$(adb shell toybox date -d 'yesterday' +%F 2>/dev/null | tr -d '\r')"

DATA="$(adb exec-out run-as "$PKG" tail -n +2 "$CSV" 2>/dev/null | tr -d '\r' || true)"
[ -n "$DATA" ] || { echo "DI-1 RESULT=FAIL (no rows)" | tee "$OUT"; exit 7; }

printf '%s\n' "$DATA" | awk -F',' -v today="$TODAY" -v yday="$YDAY" '
function date_ok(d){ return d ~ /^[0-9]{4}-[0-9]{2}-[0-9]{2}$/ }
function is_int(x){ return x ~ /^-?[0-9]+$/ }
BEGIN{fresh=0; bad=0}
{
  d=$1; ver=$2; u=$3
  if(!date_ok(d)) { bad=1; exit }
  if(ver!="v6.0") { bad=1; exit }
  if(!(is_int(u) && (u+0)>=0 && (u+0)<=20000)) { bad=1; exit }
  if(d==today || d==yday) fresh=1
}
END{
  if(bad) exit 1
  if(!fresh) exit 2
}
' || {
  rc=$?
  case "$rc" in
    1) echo "DI-1 RESULT=FAIL (type/range violation)" | tee "$OUT"; exit 8 ;;
    2) echo "DI-1 RESULT=FAIL (no row for today/yesterday)" | tee "$OUT"; exit 9 ;;
    *) echo "DI-1 RESULT=FAIL (unknown)" | tee "$OUT"; exit 10 ;;
  esac
}

echo "DI-1 RESULT=PASS" | tee "$OUT"
exit 0
