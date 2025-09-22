#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/sleep/di3.12.txt"
SUM="files/daily_sleep_summary.csv"
DUR="files/daily_sleep_duration.csv"
LSUM="app/locks/daily_sleep_summary.header"
LDUR="app/locks/daily_sleep_duration.header"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "DI-3 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG"  >/dev/null 2>&1 || { echo "DI-3 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

ESUM="$(tr -d '\r' < "$LSUM" 2>/dev/null || true)"
EDUR="$(tr -d '\r' < "$LDUR" 2>/dev/null || true)"
[ -n "$ESUM" ] || { echo "DI-3 RESULT=FAIL (missing summary lock)" | tee "$OUT"; exit 4; }
[ -n "$EDUR" ] || { echo "DI-3 RESULT=FAIL (missing duration lock)" | tee "$OUT"; exit 4; }

HSUM="$(adb exec-out run-as "$PKG" head -n1 "$SUM" 2>/dev/null | tr -d '\r' || true)"
HDUR="$(adb exec-out run-as "$PKG" head -n1 "$DUR" 2>/dev/null | tr -d '\r' || true)"
[ -n "$HSUM" ] || { echo "DI-3 RESULT=FAIL (missing summary csv)" | tee "$OUT"; exit 5; }
[ -n "$HDUR" ] || { echo "DI-3 RESULT=FAIL (missing duration csv)" | tee "$OUT"; exit 5; }
[ "$HSUM" = "$ESUM" ] || { echo "DI-3 RESULT=FAIL (summary header drift)" | tee "$OUT"; exit 6; }
[ "$HDUR" = "$EDUR" ] || { echo "DI-3 RESULT=FAIL (duration header drift)" | tee "$OUT"; exit 6; }

check_summary() {
  adb exec-out run-as "$PKG" tail -n +2 "$SUM" 2>/dev/null | tr -d '\r' |
  awk -F',' '
  function date_ok(d){ return d ~ /^[0-9]{4}-[0-9]{2}-[0-9]{2}$/ }
  function time_ok(t){ return (t=="" || t ~ /^([01][0-9]|2[0-3]):[0-5][0-9](:[0-5][0-9])?$/) }
  function is_num(x){ return x ~ /^-?[0-9]+([.][0-9]+)?$/ }
  {
    d=$1; st=$2; wt=$3; vhr=$4
    if(!date_ok(d)) exit 1
    if(!time_ok(st) || !time_ok(wt)) exit 1
    if(vhr!=""){
      if(!(is_num(vhr) && (vhr+0)>=0 && (vhr+0)<=18.0)) exit 1
    }
  }'
}

check_duration() {
  adb exec-out run-as "$PKG" tail -n +2 "$DUR" 2>/dev/null | tr -d '\r' |
  awk -F',' '
  function date_ok(d){ return d ~ /^[0-9]{4}-[0-9]{2}-[0-9]{2}$/ }
  function is_num(x){ return x ~ /^-?[0-9]+([.][0-9]+)?$/ }
  {
    d=$1; vhr=$2
    if(!date_ok(d)) exit 1
    if(vhr!=""){
      if(!(is_num(vhr) && (vhr+0)>=0 && (vhr+0)<=18.0)) exit 1
    }
  }'
}

check_summary || { echo "DI-3 RESULT=FAIL (summary invalid)" | tee "$OUT"; exit 7; }
check_duration || { echo "DI-3 RESULT=FAIL (duration invalid)" | tee "$OUT"; exit 7; }

echo "DI-3 RESULT=PASS" | tee "$OUT"
exit 0
