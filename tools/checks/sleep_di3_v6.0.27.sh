#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/sleep/di3.27.txt"
SUM="files/daily_sleep_summary.csv"
DUR="files/daily_sleep_duration.csv"
LSUM="app/locks/daily_sleep_summary.header"
LDUR="app/locks/daily_sleep_duration.header"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "DI-3 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "DI-3 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

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

# --- validate summary CSV rows (robust to quotes/extra cols/blanks) ---
adb exec-out run-as "$PKG" tail -n +2 "$SUM" 2>/dev/null | tr -d '\r' | awk -F',' '
function trimq(s){ gsub(/^ *"|" *$/,"",s); gsub(/^ +| +$/,"",s); return s }
function date_ok(d){ return d ~ /^[0-9]{4}-[0-9]{2}-[0-9]{2}$/ }
# allow "", HH:MM, HH:MM:SS, 24:00, 24:00:00
function time_ok(t){ return (t=="" || t ~ /^(([01][0-9]|2[0-3]):[0-5][0-9](:[0-5][0-9])?|24:00(:00)?)$/) }
function is_num(x){ return x ~ /^-?[0-9]+(\.[0-9]+)?$/ }
{
  # ignore pure blank/whitespace rows
  line=$0; gsub(/[[:space:]]/,"",line); if (line=="") next

  # pull first 4 fields only, clean quotes/spaces
  d = trimq($1); st = trimq($2); wt = trimq($3); hrs = trimq($4)

  if(!date_ok(d))            { exit 1 }
  if(!time_ok(st) || !time_ok(wt)) { exit 1 }
  if(hrs!=""){
    if(!is_num(hrs))         { exit 1 }
    h = hrs + 0
    if(h<0 || h>18.0)        { exit 1 }
  }
}
END { }' || { echo "DI-3 RESULT=FAIL (summary invalid)" | tee "$OUT"; exit 8; }

# --- validate duration CSV rows (robust) ---
adb exec-out run-as "$PKG" tail -n +2 "$DUR" 2>/dev/null | tr -d '\r' | awk -F',' '
function trimq(s){ gsub(/^ *"|" *$/,"",s); gsub(/^ +| +$/,"",s); return s }
function date_ok(d){ return d ~ /^[0-9]{4}-[0-9]{2}-[0-9]{2}$/ }
function is_num(x){ return x ~ /^-?[0-9]+(\.[0-9]+)?$/ }
{
  line=$0; gsub(/[[:space:]]/,"",line); if (line=="") next
  d = trimq($1); hrs = trimq($2)
  if(!date_ok(d))            { exit 1 }
  if(hrs!=""){
    if(!is_num(hrs))         { exit 1 }
    h = hrs + 0
    if(h<0 || h>18.0)        { exit 1 }
  }
}
END { }' || { echo "DI-3 RESULT=FAIL (duration invalid)" | tee "$OUT"; exit 9; }

echo "DI-3 RESULT=PASS" | tee "$OUT"
exit 0
