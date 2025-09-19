#!/usr/bin/env bash
PKG="com.nick.myrecoverytracker"

D1="2024-10-05"
D2="2024-10-06"

adb exec-out run-as "$PKG" sh -c '
mkdir -p files
for f in screen_log.csv unlock_log.csv daily_sleep_summary.csv; do
  [ -f "files/$f" ] && cp "files/$f" "files/$f.bak" || true
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

adb shell am broadcast -a "$PKG".ACTION_RUN_SLEEP_ROLLUP --ei backfill_days 500 -n "$PKG"/.TriggerReceiver >/dev/null
sleep 2
adb shell am broadcast -a "$PKG".ACTION_RUN_SLEEP_ROLLUP --ei backfill_days 500 -n "$PKG"/.TriggerReceiver >/dev/null
sleep 1

CSV="$(adb exec-out run-as "$PKG" cat files/daily_sleep_summary.csv 2>/dev/null || printf "")"
H2="$(printf "%s\n" "$CSV" | awk -F, -v d="$D2" 'NR>1&&$1==d{print $4; exit}')"
C2="$(printf "%s\n" "$CSV" | awk -F, -v d="$D2" 'NR>1&&$1==d{c++} END{print c+0}')"

START_EPOCH="$(adb shell "toybox date -d '$D1 23:31:00' +%s" | tr -d '\r')"
END_EPOCH="$(adb shell "toybox date -d '$D2 06:45:00' +%s" | tr -d '\r')"
[ -z "$START_EPOCH" ] && { echo "Sleep TC-4 RESULT=FAIL (epoch calc)"; exit 1; }
[ -z "$END_EPOCH" ] && { echo "Sleep TC-4 RESULT=FAIL (epoch calc)"; exit 1; }

EXP_H="$(awk -v a="$START_EPOCH" -v b="$END_EPOCH" 'BEGIN{printf "%.2f",(b-a)/3600.0}')"

PASS=1
[ "$C2" -eq 1 ] || PASS=0
if [ -n "$H2" ]; then
  v="${H2%\"}"; v="${v#\"}"
  awk -v x="$v" -v e="$EXP_H" 'BEGIN{d=(x-e); if(d<0)d=-d; exit !(d<=0.30)}' >/dev/null 2>&1 || PASS=0
else
  PASS=0
fi

if [ $PASS -eq 1 ]; then
  echo "Sleep TC-4 RESULT=PASS (D1=$D1, D2=$D2, expected=$EXP_H, got='${H2:-∅}', rows[D2]=$C2)"
  RC=0
else
  echo "Sleep TC-4 RESULT=FAIL (D1=$D1, D2=$D2, expected=$EXP_H, got='${H2:-∅}', rows[D2]=$C2)"
  printf "%s\n" "$CSV" | awk -F, -v a="$D1" -v b="$D2" 'NR==1||$1==a||$1==b'
  RC=1
fi

adb exec-out run-as "$PKG" sh -c '
for f in screen_log.csv unlock_log.csv daily_sleep_summary.csv; do
  [ -f "files/$f.bak" ] && mv -f "files/$f.bak" "files/$f" || true
done
' >/dev/null 2>&1 || true

exit $RC
