#!/bin/bash
PKG="com.nick.myrecoverytracker"

adb get-state >/dev/null 2>&1 || { echo "TC-2 RESULT=FAIL (no device)"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "TC-2 RESULT=FAIL (app not installed)"; exit 3; }

EPOCH="$(adb shell toybox date +%s | tr -d $'\r')"
D_TODAY="$(adb shell toybox date -d "@$EPOCH" +%F | tr -d $'\r')"
D_YEST="$(adb shell toybox date -d "@$((EPOCH-86400))" +%F | tr -d $'\r')"

get_cnt(){ awk -F, -v d="$1" 'NR>1&&$1==d{print $2;f=1;exit} END{if(!f)print 0}'; }

CSV="$(adb exec-out run-as "$PKG" cat files/daily_unlocks.csv 2>/dev/null || printf "")"
C_Y_BEFORE="$(printf '%s\n' "$CSV" | get_cnt "$D_YEST")"
C_T_BEFORE="$(printf '%s\n' "$CSV" | get_cnt "$D_TODAY")"

Y1="$D_YEST 23:59:57"; Y2="$D_YEST 23:59:59"
T1="$D_TODAY 00:00:01"; T2="$D_TODAY 00:00:03"

adb exec-out run-as "$PKG" sh -c '
mkdir -p files
[ -f files/unlock_log.csv ] || printf "ts,event\n" > files/unlock_log.csv
' >/dev/null 2>&1

for line in "$Y1" "$Y2" "$T1" "$T2"; do
  adb exec-out run-as "$PKG" sh -c "grep -Fxq '$line,UNLOCK' files/unlock_log.csv || printf '%s,UNLOCK\n' '$line' >> files/unlock_log.csv"
done

for i in 1 2 3; do
  adb shell am broadcast -a "$PKG".ACTION_RUN_UNLOCK_ROLLUP -n "$PKG"/.TriggerReceiver >/dev/null 2>&1
  sleep 2
done

CSV2="$(adb exec-out run-as "$PKG" cat files/daily_unlocks.csv 2>/dev/null || printf "")"
C_Y_AFTER="$(printf '%s\n' "$CSV2" | get_cnt "$D_YEST")"
C_T_AFTER="$(printf '%s\n' "$CSV2" | get_cnt "$D_TODAY")"

DY=$((C_Y_AFTER - C_Y_BEFORE))
DT=$((C_T_AFTER - C_T_BEFORE))

echo "TC-2 EXPECT +2 EACH — Y:$D_YEST before=$C_Y_BEFORE after=$C_Y_AFTER delta=$DY ; T:$D_TODAY before=$C_T_BEFORE after=$C_T_AFTER delta=$DT"

if [ "$DY" -eq 2 ] && [ "$DT" -eq 2 ]; then
  echo "TC-2 RESULT=PASS"
  exit 0
else
  echo "TC-2 RESULT=FAIL"
  printf "%s\n" "$CSV2" | tail -n 20
  exit 1
fi
