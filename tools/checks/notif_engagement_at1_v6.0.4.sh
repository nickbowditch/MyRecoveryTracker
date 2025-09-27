#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
CSV_DAILY="files/daily_notification_engagement.csv"
CSV_RAW="files/notification_log.csv"
ACT="$PKG.ACTION_RUN_ENGAGEMENT_ROLLUP"
CMP="$PKG/.TriggerReceiver"
OUT="evidence/v6.0/notification_engagement/at1.4.txt"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "AT-1 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG"  >/dev/null 2>&1 || { echo "AT-1 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

T="$(adb shell date +%F | tr -d '\r')"

adb exec-out run-as "$PKG" sh -c '
mkdir -p files
[ -f "'"$CSV_RAW"'" ]   || printf "ts,event,notif_id\n" >"'"$CSV_RAW"'"
[ -f "'"$CSV_DAILY"'" ] || printf "date,feature_schema_version,delivered,opened,open_rate\n" >"'"$CSV_DAILY"'"
' >/dev/null

getc(){ awk -F, -v d="$1" 'NR>1&&$1==d{print $3","$4","$5; f=1; exit} END{if(!f) print ""}'; }
cnt_raw(){ awk -F, -v d="$1" 'NR>1{ if(substr($1,1,10)==d) c++ } END{ print c+0 }'; }

before_daily="$(adb exec-out run-as "$PKG" cat "$CSV_DAILY" 2>/dev/null | tr -d '\r' | getc "$T")"
before_raw="$(adb exec-out run-as "$PKG"  cat "$CSV_RAW"   2>/dev/null | tr -d '\r' | cnt_raw "$T")"

TS1="$T 09:11:07"
TS2="$T 12:22:09"

adb exec-out run-as "$PKG" sh -c '
f="'"$CSV_RAW"'"
printf "%s,POSTED,at1-a\n%s,CLICKED,at1-a\n" "'"$TS1"'" "'"$TS2"'" >>"$f"
' >/dev/null

adb shell am broadcast -a "$ACT" -n "$CMP" >/dev/null 2>&1 || true
sleep 2
adb shell am broadcast -a "$ACT" -n "$CMP" >/dev/null 2>&1 || true
sleep 1

after_daily="$(adb exec-out run-as "$PKG" cat "$CSV_DAILY" 2>/dev/null | tr -d '\r' | getc "$T")"
after_raw="$(adb exec-out run-as "$PKG"  cat "$CSV_RAW"   2>/dev/null | tr -d '\r' | cnt_raw "$T")"

adb exec-out run-as "$PKG" sh -c '
in="'"$CSV_RAW"'"; tmp="${in}.tmp.$$"; a="'"$TS1"'"; b="'"$TS2"'"
awk -F, -v a="$a" -v b="$b" '"'"'NR==1{print;next}{ if(!($1==a && $2=="POSTED") && !($1==b && $2=="CLICKED")) print }'"'"' "$in" >"$tmp" && mv "$tmp" "$in"
' >/dev/null || true

inc_raw=$(( ${after_raw:-0} - ${before_raw:-0} ))

if [ "$inc_raw" -ge 2 ] && [ "$after_daily" != "$before_daily" ]; then
echo "AT-1 RESULT=PASS" | tee "$OUT"; exit 0
else
echo "AT-1 RESULT=FAIL (daily=$after_daily raw=$after_raw inc_raw=$inc_raw before_daily=$before_daily)" | tee "$OUT"; exit 1
fi
