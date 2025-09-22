#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/unlocks/tc2.22.txt"
CSV_DAILY="files/daily_unlocks.csv"
CSV_RAW="files/unlock_log.csv"
ACT="$PKG.ACTION_RUN_UNLOCK_ROLLUP"
CMP="$PKG/.TriggerReceiver"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "TC-2 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG"  >/dev/null 2>&1 || { echo "TC-2 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

adb exec-out run-as "$PKG" sh -c '
mkdir -p files
[ -f "'"$CSV_RAW"'" ]   || printf "ts,event\n" >"'"$CSV_RAW"'"
[ -f "'"$CSV_DAILY"'" ] || printf "date,feature_schema_version,daily_unlocks\n" >"'"$CSV_DAILY"'"
' >/dev/null

pick_free_date() {
  adb exec-out run-as "$PKG" sh -c '
    today=$(date +%F)
    for off in 0 1 2 3 4 5 6; do
      d=$(toybox date -d "$today -$off day" +%F)
      in_daily=$(awk -F, -v d="$d" "NR>1&&\$1==d{print 1;exit}" "'"$CSV_DAILY"'" )
      in_raw=$(awk -F, -v d="$d"   "NR>1{if(substr(\$1,1,10)==d){print 1;exit}}" "'"$CSV_RAW"'" )
      if [ -z "$in_daily$in_raw" ]; then echo "$d"; exit 0; fi
    done
    echo "$today"
  '
}
D="$(pick_free_date | tr -d '\r')"

getc(){ awk -F, -v d="$1" 'NR>1&&$1==d{print $NF;f=1;exit} END{if(!f)print 0}'; }
cnt_raw(){ awk -F, -v d="$1" 'NR>1{if(substr($1,1,10)==d)c++} END{print c+0}'; }

before_daily="$(adb exec-out run-as "$PKG" cat "$CSV_DAILY" | tr -d '\r' | getc "$D")"
before_raw="$(adb exec-out run-as "$PKG"  cat "$CSV_RAW"   | tr -d '\r' | cnt_raw "$D")"

S1="$(( (RANDOM % 50) + 5 ))"; S2="$(( S1 + 2 ))"
TS1="$D 13:37:$(printf '%02d' "$S1")"
TS2="$D 13:37:$(printf '%02d' "$S2")"

adb exec-out run-as "$PKG" sh -c '
f="'"$CSV_RAW"'"; a="'"$TS1"'"; b="'"$TS2"'"
printf "%s,UNLOCK\n%s,UNLOCK\n" "$a" "$b" >> "$f"
' >/dev/null

adb shell am broadcast -a "$ACT" -n "$CMP" >/dev/null 2>&1 || true
sleep 2
adb shell am broadcast -a "$ACT" -n "$CMP" >/dev/null 2>&1 || true
sleep 1

after_daily="$(adb exec-out run-as "$PKG" cat "$CSV_DAILY" | tr -d '\r' | getc "$D")"
after_raw="$(adb exec-out run-as "$PKG"  cat "$CSV_RAW"   | tr -d '\r' | cnt_raw "$D")"

adb exec-out run-as "$PKG" sh -c '
in="'"$CSV_RAW"'"; tmp="${in}.tmp.$$"; a="'"$TS1"'"; b="'"$TS2"'"
awk -F, -v a="$a" -v b="$b" '"'"'NR==1{print;next}{if($1!=a && $1!=b) print}'"'"' "$in" > "$tmp" && mv "$tmp" "$in"
' >/dev/null || true

inc_daily=$(( ${after_daily:-0} - ${before_daily:-0} ))
inc_raw=$(( ${after_raw:-0} - ${before_raw:-0} ))

if [ "$after_daily" -eq "$after_raw" ] && [ "$inc_daily" -ge 2 ]; then
  echo "TC-2 RESULT=PASS" | tee "$OUT"; exit 0
else
  echo "TC-2 RESULT=FAIL (d=$D daily=$after_daily raw=$after_raw inc_daily=$inc_daily inc_raw=$inc_raw)" | tee "$OUT"; exit 1
fi
