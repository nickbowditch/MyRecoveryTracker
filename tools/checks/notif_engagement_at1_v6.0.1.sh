#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
CSV_DAILY="files/daily_notification_engagement.csv"
ACT="$PKG.ACTION_RUN_ENGAGEMENT_ROLLUP"
CMP="$PKG/.TriggerReceiver"
OUT="evidence/v6.0/notification_engagement/at1.1.txt"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "AT-1 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG"  >/dev/null 2>&1 || { echo "AT-1 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

TODAY="$(adb shell date +%F | tr -d '\r')"
YEST="$(adb shell date -d "$TODAY -1 day" +%F 2>/dev/null | tr -d '\r' || date -v-1d +%F)"

adb exec-out run-as "$PKG" sh -c '
mkdir -p files
[ -f "'"$CSV_DAILY"'" ] || printf "date,feature_schema_version,delivered,opened,open_rate\n" >"'"$CSV_DAILY"'"
' >/dev/null

getc(){ awk -F, -v d="$1" 'NR>1&&$1==d{print $3","$4","$5; f=1; exit} END{if(!f) print ""}'; }

before_today="$(adb exec-out run-as "$PKG" cat "$CSV_DAILY" 2>/dev/null | tr -d '\r' | getc "$TODAY")"
before_yest="$(adb exec-out run-as "$PKG" cat "$CSV_DAILY" 2>/dev/null | tr -d '\r' | getc "$YEST")"

adb shell am broadcast -a "$ACT" -n "$CMP" >/dev/null 2>&1 || true
sleep 2
adb shell am broadcast -a "$ACT" -n "$CMP" >/dev/null 2>&1 || true
sleep 1

after_today="$(adb exec-out run-as "$PKG" cat "$CSV_DAILY" 2>/dev/null | tr -d '\r' | getc "$TODAY")"
after_yest="$(adb exec-out run-as "$PKG" cat "$CSV_DAILY" 2>/dev/null | tr -d '\r' | getc "$YEST")"

chg=0
[ "$before_today" != "$after_today" ] && chg=1
[ "$before_yest"  != "$after_yest" ]  && chg=1

if [ "$chg" -eq 1 ]; then
  echo "AT-1 RESULT=PASS" | tee "$OUT"; exit 0
else
  echo "AT-1 RESULT=FAIL (no change for today=$TODAY or yesterday=$YEST)" | tee "$OUT"; exit 1
fi
