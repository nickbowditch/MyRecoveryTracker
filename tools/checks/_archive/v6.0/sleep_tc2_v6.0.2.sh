#!/bin/bash
PKG="com.nick.myrecoverytracker"
adb get-state >/dev/null 2>&1 || { echo "TC-2 RESULT=FAIL (no device)"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "TC-2 RESULT=FAIL (app not installed)"; exit 3; }
EPOCH="$(adb shell date +%s | tr -d $'\r')"
D_TODAY="$(adb shell date +%F | tr -d $'\r')"
D_YEST="$(adb shell toybox date -d "@$((EPOCH-86400))" +%F | tr -d $'\r')"
CSV="$(adb exec-out run-as "$PKG" cat files/daily_sleep.csv 2>/dev/null || printf "")"
get_dur(){ awk -F, -v d="$1" 'NR>1&&$1==d{print $4;f=1;exit} END{if(!f)print 0}' <<<"$CSV"; }
Y_BEFORE="$(get_dur "$D_YEST")"
T_BEFORE="$(get_dur "$D_TODAY")"
adb exec-out run-as "$PKG" sh -c "
mkdir -p files
[ -f files/sleep_log.csv ] || printf 'ts,event\n' > files/sleep_log.csv
printf '%s,SLEEP_START\n%s,WAKE\n' '$D_YEST 23:59:30' '$D_TODAY 00:00:30' >> files/sleep_log.csv
" >/dev/null 2>&1
for i in 1 2; do adb shell am broadcast -a "$PKG".ACTION_RUN_SLEEP_ROLLUP -n "$PKG"/.TriggerReceiver >/dev/null; sleep 2; done
CSV2="$(adb exec-out run-as "$PKG" cat files/daily_sleep.csv 2>/dev/null || printf "")"
get_dur2(){ awk -F, -v d="$1" 'NR>1&&$1==d{print $4;f=1;exit} END{if(!f)print 0}' <<<"$CSV2"; }
Y_AFTER="$(get_dur2 "$D_YEST")"
T_AFTER="$(get_dur2 "$D_TODAY")"
DY="$(awk -v a="$Y_AFTER" -v b="$Y_BEFORE" 'BEGIN{print (a+0)-(b+0)}')"
DT="$(awk -v a="$T_AFTER" -v b="$T_BEFORE" 'BEGIN{print (a+0)-(b+0)}')"
echo "TC-2 DELTAS Y=$DY T=$DT"
awk -v dy="$DY" -v dt="$DT" 'BEGIN{ if(dy>0 && dt>0) exit 0; else exit 1 }' || { echo "TC-2 RESULT=FAIL"; printf "%s\n" "$CSV2" | tail -n 20; exit 1; }
echo "TC-2 RESULT=PASS"
exit 0
