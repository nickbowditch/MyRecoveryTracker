#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
CSV_DAILY="files/daily_notification_engagement.csv"
CSV_RAW="files/notification_log.csv"
ACT="$PKG.ACTION_RUN_ENGAGEMENT_ROLLUP"
CMP="$PKG/.TriggerReceiver"
OUT="evidence/v6.0/notification_engagement/at1.5.txt"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "AT-1 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG"  >/dev/null 2>&1 || { echo "AT-1 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

T="$(adb shell date +%F | tr -d '\r')"
UNIQ="$(adb shell toybox date +%s%N 2>/dev/null | tr -d '\r' || date +%s)"
ID="at1-$UNIQ"

adb exec-out run-as "$PKG" sh -c '
mkdir -p files
[ -f "'"$CSV_RAW"'" ]   || printf "ts,event,notif_id\n" >"'"$CSV_RAW"'"
[ -f "'"$CSV_DAILY"'" ] || printf "date,feature_schema_version,delivered,opened,open_rate\n" >"'"$CSV_DAILY"'"
' >/dev/null

getc(){ awk -F, -v d="$1" 'NR>1&&$1==d{print $3","$4","$5; f=1; exit} END{if(!f) print ""}'; }
cnt_raw_for_id(){ awk -F, -v d="$1" -v id="$2" 'NR>1{ if(substr($1,1,10)==d && $3==id) c++ } END{ print c+0 }'; }

before_today="$(adb exec-out run-as "$PKG" cat "$CSV_DAILY" 2>/dev/null | tr -d '\r' | getc "$T")"
before_raw_id="$(adb exec-out run-as "$PKG"  cat "$CSV_RAW"   2>/dev/null | tr -d '\r' | cnt_raw_for_id "$T" "$ID")"

TS1="$T 09:11:07"
TS2="$T 12:22:09"

adb exec-out run-as "$PKG" sh -c '
f="'"$CSV_RAW"'"
id="'"$ID"'"
printf "%s,POSTED,%s\n%s,CLICKED,%s\n" "'"$TS1"'" "$id" "'"$TS2"'" "$id" >>"$f"
' >/dev/null

adb shell am broadcast -a "$ACT" -n "$CMP" >/dev/null 2>&1 || true
sleep 2
adb shell am broadcast -a "$ACT" -n "$CMP" >/dev/null 2>&1 || true
sleep 1

after_today="$(adb exec-out run-as "$PKG" cat "$CSV_DAILY" 2>/dev/null | tr -d '\r' | getc "$T")"
after_raw_id="$(adb exec-out run-as "$PKG"  cat "$CSV_RAW"   2>/dev/null | tr -d '\r' | cnt_raw_for_id "$T" "$ID")"

adb exec-out run-as "$PKG" sh -c '
in="'"$CSV_RAW"'"; tmp="${in}.tmp.$$"; id="'"$ID"'"
awk -F, -v id="$id" '"'"'NR==1{print;next}{ if($3!=id) print }'"'"' "$in" >"$tmp" && mv "$tmp" "$in"
' >/dev/null || true

changed=0
[ "$before_today" != "$after_today" ] && changed=1

if [ "$changed" -eq 1 ] && [ "${after_raw_id:-0}" -ge 2 ]; then
  echo "AT-1 RESULT=PASS" | tee "$OUT"; exit 0
else
  echo "AT-1 RESULT=FAIL (today_row_before=$before_today today_row_after=$after_today raw_id_count=$after_raw_id id=$ID)" | tee "$OUT"; exit 1
fi
