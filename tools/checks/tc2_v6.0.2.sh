#!/bin/bash
PKG="com.nick.myrecoverytracker"

adb get-state >/dev/null 2>&1 || { echo "TC-2 RESULT=FAIL (no device)"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "TC-2 RESULT=FAIL (app not installed)"; exit 3; }

EPOCH="$(adb shell toybox date +%s | tr -d $'\r')"
D_TODAY="$(adb shell toybox date -d "@$EPOCH" +%F | tr -d $'\r')"
D_YEST="$(adb shell toybox date -d "@$((EPOCH-86400))" +%F | tr -d $'\r')"

CSV="$(adb exec-out run-as "$PKG" cat files/daily_unlocks.csv 2>/dev/null || printf "")"
get_cnt(){ awk -F, -v d="$1" 'NR>1&&$1==d{print $2;f=1;exit} END{if(!f)print 0}'; }
C_Y_BEFORE="$(printf '%s\n' "$CSV" | get_cnt "$D_YEST")"
C_T_BEFORE="$(printf '%s\n' "$CSV" | get_cnt "$D_TODAY")"

s="$((EPOCH%50))"; s2="$(( (s+1)%50 ))"
Y1="$(printf '%s 23:59:%02d' "$D_YEST"  $((58 - (s%2))) )"
Y2="$(printf '%s 23:59:%02d' "$D_YEST"  $((59 - (s%2))) )"
T1="$(printf '%s 00:00:%02d' "$D_TODAY" $s  )"
T2="$(printf '%s 00:00:%02d' "$D_TODAY" $s2 )"

adb exec-out run-as "$PKG" sh -c '
mkdir -p files
[ -f files/unlock_log.csv ] || printf "ts,event\n" > files/unlock_log.csv
' >/dev/null 2>&1

adb exec-out run-as "$PKG" sh -c "grep -Fxq '$Y1,UNLOCK' files/unlock_log.csv || printf '%s,UNLOCK\n' '$Y1' >> files/unlock_log.csv"
adb exec-out run-as "$PKG" sh -c "grep -Fxq '$Y2,UNLOCK' files/unlock_log.csv || printf '%s,UNLOCK\n' '$Y2' >> files/unlock_log.csv"
adb exec-out run-as "$PKG" sh -c "grep -Fxq '$T1,UNLOCK' files/unlock_log.csv || printf '%s,UNLOCK\n' '$T1' >> files/unlock_log.csv"
adb exec-out run-as "$PKG" sh -c "grep -Fxq '$T2,UNLOCK' files/unlock_log.csv || printf '%s,UNLOCK\n' '$T2' >> files/unlock_log.csv"

for i in 1 2 3; do
  adb shell am broadcast -a "$PKG".ACTION_RUN_UNLOCK_ROLLUP -n "$PKG"/.TriggerReceiver >/dev/null 2>&1
  sleep 2
done

CSV2="$(adb exec-out run-as "$PKG" cat files/daily_unlocks.csv 2>/dev/null || printf "")"
C_Y_AFTER="$(printf '%s\n' "$CSV2" | get_cnt "$D_YEST")"
C_T_AFTER="$(printf '%s\n' "$CSV2" | get_cnt "$D_TODAY")"

DY=$((C_Y_AFTER - C_Y_BEFORE))
DT=$((C_T_AFTER - C_T_BEFORE))

[ "$DY" -eq 2 ] && [ "$DT" -eq 2 ] && { echo "TC-2 RESULT=PASS"; exit 0; }
echo "TC-2 RESULT=FAIL"
printf "%s\n" "$CSV2" | tail -n 12
exit 1
