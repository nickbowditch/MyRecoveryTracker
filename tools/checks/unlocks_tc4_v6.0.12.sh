#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/unlocks/tc4.12.txt"
CSV_DAILY="files/daily_unlocks.csv"
CSV_RAW="files/unlock_log.csv"
ACT="$PKG.ACTION_RUN_UNLOCK_ROLLUP"
CMP="$PKG/.TriggerReceiver"
mkdir -p "$(dirname "$OUT")"; : >"$OUT"

adb get-state >/dev/null 2>&1 || { echo "TC-4 RESULT=FAIL (no device)" | tee -a "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "TC-4 RESULT=FAIL (app not installed)" | tee -a "$OUT"; exit 3; }

T="$(adb shell date +%F | tr -d '\r')"
adb exec-out run-as "$PKG" sh -c '
mkdir -p files
[ -f "'"$CSV_RAW"'" ] || printf "ts,event\n" > "'"$CSV_RAW"'"
[ -f "'"$CSV_DAILY"'" ] || printf "date,feature_schema_version,daily_unlocks\n" > "'"$CSV_DAILY"'"
' >/dev/null

get_daily(){ awk -F, -v d="$1" 'NR>1&&$1==d{print $NF;f=1;exit} END{if(!f)print 0}'; }
count_raw(){ awk -F, -v d="$1" 'NR>1{if(substr($1,1,10)==d)c++} END{print c+0}'; }

before_daily="$(adb exec-out run-as "$PKG" cat "$CSV_DAILY" 2>/dev/null | tr -d '\r' | get_daily "$T")"
before_raw="$(adb exec-out run-as "$PKG"  cat "$CSV_RAW"   2>/dev/null | tr -d '\r' | count_raw "$T")"

TS1="$T 04:13:07"; TS2="$T 14:26:19"; TS3="$T 21:39:41"
adb exec-out run-as "$PKG" sh -c '
f="'"$CSV_RAW"'"
printf "%s,UNLOCK\n%s,UNLOCK\n%s,UNLOCK\n" "'"$TS1"'" "'"$TS2"'" "'"$TS3"'" >> "$f"
' >/dev/null

adb shell am broadcast -a "$ACT" -n "$CMP" >/dev/null 2>&1 || true
sleep 2
adb shell am broadcast -a "$ACT" -n "$CMP" >/dev/null 2>&1 || true
sleep 1

after_daily="$(adb exec-out run-as "$PKG" cat "$CSV_DAILY" 2>/dev/null | tr -d '\r' | get_daily "$T")"
after_raw="$(adb exec-out run-as "$PKG"  cat "$CSV_RAW"   2>/dev/null | tr -d '\r' | count_raw "$T")"

adb exec-out run-as "$PKG" sh -c '
in="'"$CSV_RAW"'"; tmp="${in}.tmp.$$"; a="'"$TS1"'"; b="'"$TS2"'"; c="'"$TS3"'"
awk -F, -v a="$a" -v b="$b" -v c="$c" '"'"'NR==1{print;next}{if($1!=a && $1!=b && $1!=c) print}'"'"' "$in" > "$tmp" && mv "$tmp" "$in"
' >/dev/null || true

inc_daily=$(( ${after_daily:-0} - ${before_daily:-0} ))
inc_raw=$(( ${after_raw:-0} - ${before_raw:-0} ))

if [ "$after_daily" -eq "$after_raw" ] && { [ "$inc_daily" -ge 3 ] || [ "$before_daily" -eq "$after_daily" ]; }; then
  echo "TC-4 RESULT=PASS" | tee -a "$OUT"; exit 0
else
  echo "TC-4 RESULT=FAIL (daily=$after_daily raw=$after_raw inc_daily=$inc_daily inc_raw=$inc_raw)" | tee -a "$OUT"; exit 1
fi
