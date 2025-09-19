#!/bin/bash
PKG="com.nick.myrecoverytracker"

adb get-state >/dev/null 2>&1 || { echo "TC-2 RESULT=FAIL (no device)"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "TC-2 RESULT=FAIL (app not installed)"; exit 3; }

EPOCH="$(adb shell toybox date +%s | tr -d $'\r')"
YSECS=$((EPOCH - 86400))
D_TODAY="$(adb shell toybox date -d "@$EPOCH" +%F | tr -d $'\r')"
D_YEST="$(adb shell toybox date -d "@$YSECS" +%F | tr -d $'\r')"

get_count() {
  d="$1"
  adb exec-out run-as "$PKG" sh -c 'cat files/daily_unlocks.csv 2>/dev/null' \
    | awk -F, -v d="$d" 'NR>1 && $1==d {print ($2+0); f=1; exit} END{if(!f) print 0}'
}

C_Y_BEFORE="$(get_count "$D_YEST")"
C_T_BEFORE="$(get_count "$D_TODAY")"

L1="$D_YEST 23:59:57,UNLOCK"
L2="$D_YEST 23:59:59,UNLOCK"
L3="$D_TODAY 00:00:01,UNLOCK"
L4="$D_TODAY 00:00:03,UNLOCK"

adb shell run-as "$PKG" sh -c '
mkdir -p files
[ -f files/unlock_log.csv ] || printf "ts,event\n" > files/unlock_log.csv
line1='"'"$L1"'"'
line2='"'"$L2"'"'
line3='"'"$L3"'"'
line4='"'"$L4"'"'
for line in "$line1" "$line2" "$line3" "$line4"; do
  grep -Fxq "$line" files/unlock_log.csv || printf "%s\n" "$line" >> files/unlock_log.csv
done
rm -f files/unlock_cursor.txt files/unlock_cursor state/unlock_cursor.txt files/unlock_rollup.state files/unlock_checkpoint.txt 2>/dev/null || true
'

for i in 1 2 3; do
  adb shell am broadcast -a "$PKG".ACTION_RUN_UNLOCK_ROLLUP -n "$PKG"/.TriggerReceiver >/dev/null 2>&1 || true
  sleep 3
done

C_Y_AFTER="$(get_count "$D_YEST")"
C_T_AFTER="$(get_count "$D_TODAY")"

DY=$((C_Y_AFTER - C_Y_BEFORE))
DT=$((C_T_AFTER - C_T_BEFORE))

if [ "$DY" -eq 2 ] && [ "$DT" -eq 2 ]; then
  echo "TC-2 RESULT=PASS"
else
  echo "TC-2 RESULT=FAIL"
  echo "yesterday($D_YEST): before=$C_Y_BEFORE after=$C_Y_AFTER delta=$DY"
  echo "today    ($D_TODAY): before=$C_T_BEFORE after=$C_T_AFTER delta=$DT"
  echo "--- daily_unlocks (last 20) ---"
  adb exec-out run-as "$PKG" sh -c 'tail -n 20 files/daily_unlocks.csv 2>/dev/null' || true
  echo "--- unlock_log (last 20) ---"
  adb exec-out run-as "$PKG" sh -c 'tail -n 20 files/unlock_log.csv 2>/dev/null' || true
  exit 1
fi
