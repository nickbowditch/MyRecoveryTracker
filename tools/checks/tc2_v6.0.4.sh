#!/bin/bash
PKG="com.nick.myrecoverytracker"

adb get-state >/dev/null 2>&1 || { echo "TC-2 RESULT=FAIL (no device)"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "TC-2 RESULT=FAIL (app not installed)"; exit 3; }

EPOCH="$(adb shell toybox date +%s | tr -d $'\r')"
D_TODAY="$(adb shell toybox date -d "@$EPOCH" +%F | tr -d $'\r')"
D_YEST="$(adb shell toybox date -d "@$((EPOCH-86400))" +%F | tr -d $'\r')"

get_cnt(){ awk -F, -v d="$1" 'NR>1&&$1==d{print $2;f=1;exit} END{if(!f)print 0}'; }

adb shell run-as "$PKG" sh -c '
set -e
mkdir -p files/_tc2
for f in files/unlock_log.csv files/daily_unlocks.csv files/unlock_cursor files/unlock_cursor.txt state/unlock_cursor.txt files/unlock_rollup.state files/unlock_checkpoint.txt; do
  [ -f "$f" ] && mv "$f" "files/_tc2/$(basename "$f")" || true
done
printf "ts,event\n" > files/unlock_log.csv
'

Y1="$D_YEST 23:59:57"; Y2="$D_YEST 23:59:59"; T1="$D_TODAY 00:00:01"; T2="$D_TODAY 00:00:03"
adb shell run-as "$PKG" sh -c "printf '%s,UNLOCK\n%s,UNLOCK\n%s,UNLOCK\n%s,UNLOCK\n' '$Y1' '$Y2' '$T1' '$T2' >> files/unlock_log.csv"

for i in 1 2 3; do
  adb shell am broadcast -a "$PKG".ACTION_RUN_UNLOCK_ROLLUP -n "$PKG"/.TriggerReceiver >/dev/null 2>&1
  sleep 2
done

CSV="$(adb exec-out run-as "$PKG" cat files/daily_unlocks.csv 2>/dev/null || printf "")"
C_Y="$(printf '%s\n' "$CSV" | get_cnt "$D_YEST")"
C_T="$(printf '%s\n' "$CSV" | get_cnt "$D_TODAY")"

if [ "$C_Y" -eq 2 ] && [ "$C_T" -eq 2 ]; then
  echo "TC-2 RESULT=PASS"
  RES=0
else
  echo "TC-2 RESULT=FAIL"
  printf "%s\n" "$CSV" | tail -n 20
  RES=1
fi

adb shell run-as "$PKG" sh -c '
set -e
for f in unlock_log.csv daily_unlocks.csv unlock_cursor unlock_cursor.txt unlock_rollup.state unlock_checkpoint.txt; do
  if [ -f "files/_tc2/$f" ]; then mv -f "files/_tc2/$f" "files/$f"; else rm -f "files/$f"; fi
done
rmdir files/_tc2 2>/dev/null || true
'

exit $RES
