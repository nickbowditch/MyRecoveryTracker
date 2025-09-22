#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
CSV_DAILY="files/daily_unlocks.csv"
CSV_RAW="files/unlock_log.csv"
ACT="$PKG.ACTION_RUN_UNLOCK_ROLLUP"
CMP="$PKG/.TriggerReceiver"
OUT="evidence/v6.0/unlocks/at1.5.txt"

adb get-state >/dev/null 2>&1 || { echo "AT-1 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "AT-1 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

T="$(adb shell date +%F | tr -d '\r')"

adb exec-out run-as "$PKG" sh -c '
mkdir -p files
[ -f "'"$CSV_RAW"'" ]   || printf "ts,event\n" >"'"$CSV_RAW"'"
[ -f "'"$CSV_DAILY"'" ] || printf "date,feature_schema_version,daily_unlocks\n" >"'"$CSV_DAILY"'"
' >/dev/null

getc(){ awk -F, -v d="$1" 'NR>1&&$1==d{print $NF; f=1; exit} END{if(!f) print 0}'; }
cnt_raw(){ awk -F, -v d="$1" 'NR>1{ if(substr($1,1,10)==d) c++ } END{ print c+0 }'; }

before_daily="$(adb exec-out run-as "$PKG" cat "$CSV_DAILY" 2>/dev/null | tr -d '\r' | getc "$T")"
before_raw="$(adb exec-out run-as "$PKG"  cat "$CSV_RAW"   2>/dev/null | tr -d '\r' | cnt_raw "$T")"

TS1="$T 09:11:07"
TS2="$T 12:22:09"

adb exec-out run-as "$PKG" sh -c '
f="'"$CSV_RAW"'"
printf "%s,UNLOCK\n%s,UNLOCK\n" "'"$TS1"'" "'"$TS2"'" >>"$f"
' >/dev/null

adb shell am broadcast -a "$ACT" -n "$CMP" >/dev/null 2>&1 || true
sleep 2
adb shell am broadcast -a "$ACT" -n "$CMP" >/dev/null 2>&1 || true
sleep 1

after_daily="$(adb exec-out run-as "$PKG" cat "$CSV_DAILY" 2>/dev/null | tr -d '\r' | getc "$T")"
after_raw="$(adb exec-out run-as "$PKG"  cat "$CSV_RAW"   2>/dev/null | tr -d '\r' | cnt_raw "$T")"

adb exec-out run-as "$PKG" sh -c '
in="'"$CSV_RAW"'"; tmp="${in}.tmp.$$"; a="'"$TS1"'"; b="'"$TS2"'"
awk -F, -v a="$a" -v b="$b" '"'"'NR==1{print;next}{ if($1!=a && $1!=b) print }'"'"' "$in" >"$tmp" && mv "$tmp" "$in"
' >/dev/null || true

inc_raw=$(( ${after_raw:-0} - ${before_raw:-0} ))

if [ "$inc_raw" -ge 2 ] && [ "$after_daily" -eq "$after_raw" ]; then
echo "AT-1 RESULT=PASS" | tee "$OUT"; exit 0
else
echo "AT-1 RESULT=FAIL (daily=$after_daily raw=$after_raw inc_raw=$inc_raw before_daily=$before_daily)" | tee "$OUT"; exit 1
fi
