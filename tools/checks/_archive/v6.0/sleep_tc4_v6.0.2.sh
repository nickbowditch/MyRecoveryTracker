#!/bin/bash
PKG="com.nick.myrecoverytracker"
D1="2025-04-05"
D2="2025-04-06"
adb get-state >/dev/null 2>&1 || { echo "TC-4 RESULT=FAIL (no device)"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "TC-4 RESULT=FAIL (app not installed)"; exit 3; }
CSV="$(adb exec-out run-as "$PKG" cat files/daily_sleep.csv 2>/dev/null || printf "")"
get_dur(){ awk -F, -v d="$1" 'NR>1&&$1==d{print $4;f=1;exit} END{if(!f)print 0}' <<<"$CSV"; }
B1="$(get_dur "$D1")"
B2="$(get_dur "$D2")"
adb exec-out run-as "$PKG" sh -c '
d1="'"$D1"'"; d2="'"$D2"'"; f=files/sleep_log.csv
mkdir -p files
[ -f "$f" ] || printf "ts,event\n" > "$f"
printf "%s,SLEEP_START\n%s,WAKE\n" "$d1 01:30:00" "$d1 02:00:00" >> "$f"
printf "%s,SLEEP_START\n%s,WAKE\n" "$d1 02:10:00" "$d1 02:40:00" >> "$f"
printf "%s,SLEEP_START\n%s,WAKE\n" "$d1 02:50:00" "$d1 03:20:00" >> "$f"
printf "%s,SLEEP_START\n%s,WAKE\n" "$d2 01:30:00" "$d2 02:00:00" >> "$f"
printf "%s,SLEEP_START\n%s,WAKE\n" "$d2 02:10:00" "$d2 02:40:00" >> "$f"
printf "%s,SLEEP_START\n%s,WAKE\n" "$d2 02:50:00" "$d2 03:20:00" >> "$f"
' >/dev/null 2>&1
for i in 1 2; do adb shell am broadcast -a "$PKG".ACTION_RUN_SLEEP_ROLLUP -n "$PKG"/.TriggerReceiver >/dev/null; sleep 2; done
CSV2="$(adb exec-out run-as "$PKG" cat files/daily_sleep.csv 2>/dev/null || printf "")"
get_dur2(){ awk -F, -v d="$1" 'NR>1&&$1==d{print $4;f=1;exit} END{if(!f)print 0}' <<<"$CSV2"; }
A1="$(get_dur2 "$D1")"
A2="$(get_dur2 "$D2")"
D1_DELTA="$(awk -v a="$A1" -v b="$B1" 'BEGIN{print (a+0)-(b+0)}')"
D2_DELTA="$(awk -v a="$A2" -v b="$B2" 'BEGIN{print (a+0)-(b+0)}')"
echo "TC-4 DELTAS D1=$D1_DELTA D2=$D2_DELTA"
awk -v x="$D1_DELTA" -v y="$D2_DELTA" 'BEGIN{ if(x>0 && y>0) exit 0; else exit 1 }' || { echo "TC-4 RESULT=FAIL"; exit 1; }
echo "TC-4 RESULT=PASS"
exit 0
