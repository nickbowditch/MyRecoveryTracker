#!/bin/bash
PKG="com.nick.myrecoverytracker"
D1="2025-04-05"
D2="2025-04-06"
adb get-state >/dev/null 2>&1 || { echo "TC-4 RESULT=FAIL (no device)"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "TC-4 RESULT=FAIL (app not installed)"; exit 3; }

CSV_B="$(adb exec-out run-as "$PKG" cat files/daily_sleep.csv 2>/dev/null || printf "")"
get_dur(){ awk -F, -v d="$1" 'NR>1&&$1==d{print $4;f=1;exit} END{if(!f)print 0}'; }
B1="$(get_dur "$D1" <<<"$CSV_B")"
B2="$(get_dur "$D2" <<<"$CSV_B")"

adb exec-out run-as "$PKG" sh -c '
d1="'"$D1"'"; d2="'"$D2"'"
mkdir -p files
[ -f files/sleep_log.csv ] || printf "ts,event\n" > files/sleep_log.csv
[ -f files/sleep_events.csv ] || printf "ts,event\n" > files/sleep_events.csv
printf "%s,SLEEP_START\n%s,WAKE\n" "$d1 01:00:00" "$d1 04:00:00" >> files/sleep_log.csv
printf "%s,SLEEP\n%s,WAKE\n"        "$d1 01:00:00" "$d1 04:00:00" >> files/sleep_events.csv
printf "%s,SLEEP_START\n%s,WAKE\n" "$d2 01:00:00" "$d2 04:00:00" >> files/sleep_log.csv
printf "%s,SLEEP\n%s,WAKE\n"        "$d2 01:00:00" "$d2 04:00:00" >> files/sleep_events.csv
' >/dev/null 2>&1

for a in "$PKG".ACTION_RUN_SLEEP_ROLLUP "$PKG".ACTION_RUN_ROLLUP_SLEEP; do
  adb shell am broadcast -a "$a" -n "$PKG"/.TriggerReceiver >/dev/null 2>&1
done
sleep 3

CSV_A="$(adb exec-out run-as "$PKG" cat files/daily_sleep.csv 2>/dev/null || printf "")"
A1="$(get_dur "$D1" <<<"$CSV_A")"
A2="$(get_dur "$D2" <<<"$CSV_A")"

DX1="$(awk -v a="$A1" -v b="$B1" 'BEGIN{print (a+0)-(b+0)}')"
DX2="$(awk -v a="$A2" -v b="$B2" 'BEGIN{print (a+0)-(b+0)}')"
echo "TC-4 DELTAS D1=$DX1 D2=$DX2"

awk -v x="$DX1" -v y="$DX2" 'BEGIN{ if(x>0 && y>0) exit 0; else exit 1 }' || { echo "TC-4 RESULT=FAIL"; exit 1; }
echo "TC-4 RESULT=PASS"
exit 0
