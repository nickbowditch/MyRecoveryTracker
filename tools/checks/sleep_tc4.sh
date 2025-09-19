#!/usr/bin/env bash
PKG="com.nick.myrecoverytracker"
BACKFILL_DAYS="${BACKFILL_DAYS:-30}"

find_dst_boundary_within() {
  now="$(date +%s)"
  for i in $(seq 1 "$BACKFILL_DAYS"); do
    d2="$(date -r $((now - i*86400)) +%F)"
    d1="$(date -r $((now - (i+1)*86400)) +%F)"
    off2="$(adb shell "toybox date -d '$d2 01:00:00' +%z" | tr -d '\r')"
    off1="$(adb shell "toybox date -d '$d1 01:00:00' +%z" | tr -d '\r')"
    [ -n "$off1" ] && [ -n "$off2" ] || continue
    if [ "$off1" != "$off2" ]; then
      echo "$d1,$d2"
      return 0
    fi
  done
  echo ""
  return 1
}

PAIR="$(find_dst_boundary_within)"
if [ -z "$PAIR" ]; then
  echo "Sleep TC-4 RESULT=SKIP (no DST boundary within last $BACKFILL_DAYS days)"
  exit 0
fi

D1="${PAIR%,*}"
D2="${PAIR#*,}"

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

START_EPOCH="$(adb shell "toybox date -d '$D1 23:31:00' +%s" | tr -d '\r')"
END_EPOCH="$(adb shell "toybox date -d '$D2 06:45:00' +%s" | tr -d '\r')"
if [ -z "$START_EPOCH" ] || [ -z "$END_EPOCH" ]; then
  echo "Sleep TC-4 RESULT=FAIL (epoch calc)"
  exit 1
fi
EXP_H="$(awk -v a="$START_EPOCH" -v b="$END_EPOCH" 'BEGIN{printf "%.2f",(b-a)/3600.0}')"

PASS=1
[ "$C2" -eq 1 ] || PASS=0
RANGE_OK=0
if [ -n "$H2" ]; then v="${H2%\"}"; v="${v#\"}"; awk -v x="$v" -v e="$EXP_H" 'BEGIN{d=(x-e); if(d<0)d=-d; exit !(d>0.26)}' >/dev/null 2>&1 || RANGE_OK=1; fi
[ $RANGE_OK -eq 1 ] || PASS=0
[ -z "$H1" ] || [ "$H1" = "0" ] || [ "$H1" = "0.00" ] || PASS=0

if [ $PASS -eq 1 ]; then
  echo "Sleep TC-4 RESULT=PASS (D1=$D1, D2=$D2, expected=${EXP_H}, got='${H2:-∅}', rows[D2]=$C2)"
  RC=0
else
  echo "Sleep TC-4 RESULT=FAIL (D1=$D1, D2=$D2, expected=${EXP_H}, got='${H2:-∅}', rows[D2]=$C2)"
  printf "%s\n" "$CSV" | awk -F, -v a="$D1" -v b="$D2" 'NR==1||$1==a||$1==b'
  RC=1
fi

adb exec-out run-as "$PKG" sh -c '
for f in screen_log.csv unlock_log.csv daily_sleep_summary.csv; do
  [ -f files/$f.bak ] && mv -f "files/$f.bak" "files/$f" || true
done
' >/dev/null 2>&1 || true

exit $RC
