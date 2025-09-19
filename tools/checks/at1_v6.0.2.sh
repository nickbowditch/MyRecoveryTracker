#!/bin/bash
PKG="com.nick.myrecoverytracker"
adb get-state >/dev/null 2>&1 || { echo "AT-1 RESULT=FAIL (no device)"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "AT-1 RESULT=FAIL (app not installed)"; exit 3; }

TODAY="$(adb shell 'date "+%F"' | tr -d $'\r')"

get_cnt(){ awk -F, -v d="$1" 'NR>1&&$1==d{print $(NF); f=1; exit} END{if(!f)print 0}'; }

CSV="$(adb exec-out run-as "$PKG" cat files/daily_unlocks.csv 2>/dev/null || printf "")"
C_BEFORE="$(printf '%s\n' "$CSV" | get_cnt "$TODAY")"

EPOCH="$(adb shell 'date +%s' | tr -d $'\r')"
TS1="$(adb shell "toybox date -d '@$EPOCH' '+%F %T'" | tr -d $'\r')"
TS2="$(adb shell "toybox date -d '@$((EPOCH+2))' '+%F %T'" | tr -d $'\r')"

adb exec-out run-as "$PKG" sh -c '
mkdir -p files
[ -f files/unlock_log.csv ] || printf "ts,event\n" > files/unlock_log.csv
' >/dev/null 2>&1
adb exec-out run-as "$PKG" sh -c "printf '%s,UNLOCK\n%s,UNLOCK\n' '$TS1' '$TS2' >> files/unlock_log.csv"

for i in 1 2; do
adb shell am broadcast -a "$PKG".ACTION_RUN_UNLOCK_ROLLUP -n "$PKG"/.TriggerReceiver >/dev/null 2>&1
sleep 2
done

CSV2="$(adb exec-out run-as "$PKG" cat files/daily_unlocks.csv 2>/dev/null || printf "")"
C_AFTER="$(printf '%s\n' "$CSV2" | get_cnt "$TODAY")"
D=$((C_AFTER - C_BEFORE))

if [ "$D" -ge 1 ]; then
echo "AT-1 RESULT=PASS"
exit 0
else
echo "AT-1 RESULT=FAIL"
echo "TODAY=$TODAY before=$C_BEFORE after=$C_AFTER delta=$D"
exit 1
fi
