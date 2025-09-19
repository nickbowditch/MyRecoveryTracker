#!/usr/bin/env bash
PKG="com.nick.myrecoverytracker"

NOW="$(date +%s)"
D2="$(date -r $((NOW-4*86400)) +%F)"
D1="$(date -r $((NOW-5*86400)) +%F)"

adb exec-out run-as "$PKG" sh -c '
mkdir -p files
for f in screen_log.csv unlock_log.csv daily_sleep_summary.csv; do
  [ -f files/$f ] && cp "files/$f" "files/$f.bak" || true
done
printf "ts,event\n" > files/screen_log.csv
printf "ts,event\n" > files/unlock_log.csv
printf "date,sleep_time,wake_time,duration_hours\n" > files/daily_sleep_summary.csv
'

adb exec-out run-as "$PKG" sh -c "
printf '%s,SCREEN_OFF\n' '$D1 23:30:00' >> files/screen_log.csv
printf '%s,LOCK\n'       '$D1 23:31:00' >> files/unlock_log.csv
printf '%s,UNLOCK\n'     '$D2 06:45:00' >> files/unlock_log.csv
printf '%s,SCREEN_ON\n'  '$D2 06:46:00' >> files/screen_log.csv
"

adb shell am broadcast -a "$PKG".ACTION_RUN_SLEEP_ROLLUP -n "$PKG"/.TriggerReceiver >/dev/null
sleep 2
adb shell am broadcast -a "$PKG".ACTION_RUN_SLEEP_ROLLUP -n "$PKG"/.TriggerReceiver >/dev/null
sleep 1

CSV="$(adb exec-out run-as "$PKG" cat files/daily_sleep_summary.csv 2>/dev/null || printf "")"
H1="$(printf "%s\n" "$CSV" | awk -F, -v d="$D1" 'NR>1&&$1==d{print $4; exit}')"
H2="$(printf "%s\n" "$CSV" | awk -F, -v d="$D2" 'NR>1&&$1==d{print $4; exit}')"
C2="$(printf "%s\n" "$CSV" | awk -F, -v d="$D2" 'NR>1&&$1==d{c++} END{print c+0}')"

PASS=1
[ "$C2" -eq 1 ] || PASS=0

RANGE_OK=0
if [ -n "$H2" ]; then
  v="${H2%\"}"; v="${v#\"}"
  awk -v x="$v" 'BEGIN{exit !(x>=6.0 && x<=10.0)}' >/dev/null 2>&1 && RANGE_OK=1
fi
[ $RANGE_OK -eq 1 ] || PASS=0

D1_OK=0
if [ -z "$H1" ] || [ "$H1" = "0" ] || [ "$H1" = "0.00" ]; then D1_OK=1; fi
[ $D1_OK -eq 1 ] || PASS=0

if [ $PASS -eq 1 ]; then
  echo "Sleep TC-2 RESULT=PASS (D1=$D1 hours='${H1:-∅}', D2=$D2 hours='${H2:-∅}', rows[D2]=$C2)"
  RC=0
else
  echo "Sleep TC-2 RESULT=FAIL"
  printf "%s\n" "$CSV" | awk -F, -v a="$D1" -v b="$D2" 'NR==1||$1==a||$1==b'
  RC=1
fi

adb exec-out run-as "$PKG" sh -c '
for f in screen_log.csv unlock_log.csv daily_sleep_summary.csv; do
  [ -f files/$f.bak ] && mv -f "files/$f.bak" "files/$f" || true
done
' >/dev/null 2>&1 || true

exit $RC
