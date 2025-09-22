#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/unlocks/tc2.18.txt"
CSV_DAILY="files/daily_unlocks.csv"
CSV_RAW="files/unlock_log.csv"
ACT="$PKG.ACTION_RUN_UNLOCK_ROLLUP"
CMP="$PKG/.TriggerReceiver"
mkdir -p "$(dirname "$OUT")"; : >"$OUT"

adb get-state >/dev/null 2>&1 || { echo "TC-2 RESULT=FAIL (no device)" | tee -a "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "TC-2 RESULT=FAIL (app not installed)" | tee -a "$OUT"; exit 3; }

T="$(adb shell date +%F | tr -d '\r')"
adb exec-out run-as "$PKG" sh -c '
mkdir -p files
[ -f "'"$CSV_RAW"'" ] || printf "ts,event\n" > "'"$CSV_RAW"'"
[ -f "'"$CSV_DAILY"'" ] || printf "date,feature_schema_version,daily_unlocks\n" > "'"$CSV_DAILY"'"
' >/dev/null

getc(){ awk -F, -v d="$1" 'NR>1&&$1==d{print $NF;f=1;exit} END{if(!f)print 0}'; }
before="$(adb exec-out run-as "$PKG" cat "$CSV_DAILY" 2>/dev/null | tr -d '\r' | getc "$T")"

TS1="$T 00:00:01"; TS2="$T 00:00:03"
adb exec-out run-as "$PKG" sh -c '
f="'"$CSV_RAW"'"
printf "%s,UNLOCK\n%s,UNLOCK\n" "'"$TS1"'" "'"$TS2"'" >> "$f"
' >/dev/null

adb shell am broadcast -a "$ACT" -n "$CMP" >/dev/null 2>&1 || true
sleep 2
adb shell am broadcast -a "$ACT" -n "$CMP" >/dev/null 2>&1 || true
sleep 1

after="$(adb exec-out run-as "$PKG" cat "$CSV_DAILY" 2>/dev/null | tr -d '\r' | getc "$T")"
delta=$(( ${after:-0} - ${before:-0} ))

adb exec-out run-as "$PKG" sh -c '
in="'"$CSV_RAW"'"; tmp="${in}.tmp.$$"; a="'"$TS1"'"; b="'"$TS2"'"
awk -F, -v a="$a" -v b="$b" 'NR==1{print;next}{if($1!=a && $1!=b) print}' "$in" > "$tmp" && mv "$tmp" "$in"
' >/dev/null || true

if [ "$delta" -ge 2 ]; then
echo "TC-2 RESULT=PASS" | tee -a "$OUT"; exit 0
else
echo "TC-2 RESULT=FAIL (today_delta=$delta)" | tee -a "$OUT"; exit 1
fi
