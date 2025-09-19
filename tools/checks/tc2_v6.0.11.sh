#!/bin/bash
PKG="com.nick.myrecoverytracker"

adb get-state >/dev/null 2>&1 || { echo "TC-2 RESULT=FAIL (no device)"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "TC-2 RESULT=FAIL (app not installed)"; exit 3; }

EPOCH="$(adb shell toybox date +%s | tr -d $'\r')"
[ -n "$EPOCH" ] || { echo "TC-2 RESULT=FAIL (no epoch)"; exit 4; }
YSECS=$((EPOCH - 86400))
D_TODAY="$(adb shell toybox date -d "@$EPOCH" +%F | tr -d $'\r')"
D_YEST="$(adb shell toybox date -d "@$YSECS" +%F | tr -d $'\r')"

get_count() {
  d="$1"
  adb exec-out run-as "$PKG" sh -c 'cat files/daily_unlocks.csv 2>/dev/null' \
    | awk -F, -v d="$d" 'NR>1 && $1==d {print ($2+0); f=1; exit} END{if(!f) print 0}'
}

adb exec-out run-as "$PKG" sh -c 'mkdir -p files && touch files/_w && rm -f files/_w' >/dev/null 2>&1 \
  || { echo "TC-2 RESULT=FAIL (files/ not writable)"; exit 5; }

adb exec-out run-as "$PKG" sh -s <<'SH'
set -e
mkdir -p files/_tc2
for f in files/unlock_log.csv files/daily_unlocks.csv files/unlock_cursor files/unlock_cursor.txt state/unlock_cursor.txt files/unlock_rollup.state files/unlock_checkpoint.txt; do
  [ -f "$f" ] && mv "$f" "files/_tc2/$(basename "$f")" || true
done
printf 'ts,event\n' > files/unlock_log.csv
printf 'date,count\n' > files/daily_unlocks.csv
SH

L1="$D_YEST 23:59:57,UNLOCK"
L2="$D_YEST 23:59:59,UNLOCK"
L3="$D_TODAY 00:00:01,UNLOCK"
L4="$D_TODAY 00:00:03,UNLOCK"

adb exec-out run-as "$PKG" sh -c "printf '%s\n' \"$L1\" >> files/unlock_log.csv"
adb exec-out run-as "$PKG" sh -c "printf '%s\n' \"$L2\" >> files/unlock_log.csv"
adb exec-out run-as "$PKG" sh -c "printf '%s\n' \"$L3\" >> files/unlock_log.csv"
adb exec-out run-as "$PKG" sh -c "printf '%s\n' \"$L4\" >> files/unlock_log.csv"

for i in 1 2 3; do
  adb shell am broadcast -a "$PKG".ACTION_RUN_UNLOCK_ROLLUP -n "$PKG"/.TriggerReceiver >/dev/null 2>&1 || true
  sleep 3
done

C_Y="$(get_count "$D_YEST")"
C_T="$(get_count "$D_TODAY")"
echo "TC-2 COUNTS — $D_YEST=$C_Y, $D_TODAY=$C_T"

RES=1
if [ "$C_Y" -eq 2 ] && [ "$C_T" -eq 2 ]; then
  echo "TC-2 RESULT=PASS"
  RES=0
else
  echo "TC-2 RESULT=FAIL"
fi

adb exec-out run-as "$PKG" sh -s <<'SH'
set -e
for f in unlock_log.csv daily_unlocks.csv unlock_cursor unlock_cursor.txt unlock_rollup.state unlock_checkpoint.txt; do
  if [ -f "files/_tc2/$f" ]; then
    mv -f "files/_tc2/$f" "files/$f"
  else
    rm -f "files/$f"
  fi
done
rmdir files/_tc2 2>/dev/null || true
SH

exit $RES
