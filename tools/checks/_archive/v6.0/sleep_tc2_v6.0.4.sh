#!/bin/bash
PKG="com.nick.myrecoverytracker"
adb get-state >/dev/null 2>&1 || { echo "TC-2 RESULT=FAIL (no device)"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "TC-2 RESULT=FAIL (app not installed)"; exit 3; }

EPOCH="$(adb shell date +%s | tr -d $'\r')"
D_TODAY="$(adb shell date +%F | tr -d $'\r')"
D_YEST="$(adb shell toybox date -d "@$((EPOCH-86400))" +%F | tr -d $'\r')"

CSV_B="$(adb exec-out run-as "$PKG" cat files/daily_sleep.csv 2>/dev/null || printf "")"
get_dur(){ awk -F, -v d="$1" 'NR>1&&$1==d{print $4;f=1;exit} END{if(!f)print 0}'; }
Y_BEFORE="$(get_dur "$D_YEST" <<<"$CSV_B")"
T_BEFORE="$(get_dur "$D_TODAY" <<<"$CSV_B")"

adb exec-out run-as "$PKG" sh -c '
mkdir -p files
[ -f files/sleep_log.csv ] || printf "ts,event\n" > files/sleep_log.csv
[ -f files/sleep_events.csv ] || printf "ts,event\n" > files/sleep_events.csv
printf "%s,SLEEP_START\n%s,WAKE\n" "'"$D_YEST"' 23:00:00" "'"$D_TODAY"' 07:00:00" >> files/sleep_log.csv
printf "%s,SLEEP\n%s,WAKE\n"        "'"$D_YEST"' 23:00:00" "'"$D_TODAY"' 07:00:00" >> files/sleep_events.csv
' >/dev/null 2>&1

for a in "$PKG".ACTION_RUN_SLEEP_ROLLUP "$PKG".ACTION_RUN_ROLLUP_SLEEP; do
  adb shell am broadcast -a "$a" -n "$PKG"/.TriggerReceiver >/dev/null 2>&1
done
sleep 3

CSV_A="$(adb exec-out run-as "$PKG" cat files/daily_sleep.csv 2>/dev/null || printf "")"
Y_AFTER="$(get_dur "$D_YEST" <<<"$CSV_A")"
T_AFTER="$(get_dur "$D_TODAY" <<<"$CSV_A")"

DY="$(awk -v a="$Y_AFTER" -v b="$Y_BEFORE" 'BEGIN{print (a+0)-(b+0)}')"
DT="$(awk -v a="$T_AFTER" -v b="$T_BEFORE" 'BEGIN{print (a+0)-(b+0)}')"
echo "TC-2 DELTAS Y=$DY T=$DT"

awk -v dy="$DY" -v dt="$DT" 'BEGIN{ if(dy>0 && dt>0) exit 0; else exit 1 }' || { echo "TC-2 RESULT=FAIL"; printf "%s\n" "$CSV_A" | tail -n 20; exit 1; }
echo "TC-2 RESULT=PASS"
exit 0
