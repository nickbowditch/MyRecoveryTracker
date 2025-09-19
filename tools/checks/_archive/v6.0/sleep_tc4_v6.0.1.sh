#!/bin/bash
PKG="com.nick.myrecoverytracker"
D1="2025-04-05"
D2="2025-04-06"

DAILY="$(adb exec-out run-as "$PKG" cat files/daily_sleep.csv 2>/dev/null || printf "")"
RAW="$(adb exec-out run-as "$PKG" cat files/sleep_log.csv 2>/dev/null || printf "")"

get_daily(){ awk -F, -v dd="$1" 'NR>1&&$1==dd{print $(NF);f=1;exit} END{if(!f)print 0}'; }
D1_BEFORE="$(printf '%s\n' "$DAILY" | get_daily "$D1")"
D2_BEFORE="$(printf '%s\n' "$DAILY" | get_daily "$D2")"

adb exec-out run-as "$PKG" sh -c '
d1="'"$D1"'"; d2="'"$D2"'"; f=files/sleep_log.csv
mkdir -p files
[ -f "$f" ] || printf "ts,event\n" > "$f"
printf "%s,SLEEP\n%s,SLEEP\n%s,SLEEP\n" "$d1 01:30:00" "$d1 02:00:00" "$d1 02:30:00" >> "$f"
printf "%s,SLEEP\n%s,SLEEP\n%s,SLEEP\n" "$d2 01:30:00" "$d2 02:00:00" "$d2 02:30:00" >> "$f"
' >/dev/null 2>&1

for i in 1 2; do
  adb shell am broadcast -a "$PKG".ACTION_RUN_SLEEP_ROLLUP -n "$PKG"/.TriggerReceiver >/dev/null
  sleep 2
done

DAILY2="$(adb exec-out run-as "$PKG" cat files/daily_sleep.csv 2>/dev/null || printf "")"
RAW2="$(adb exec-out run-as "$PKG" cat files/sleep_log.csv 2>/dev/null || printf "")"

raw_count(){ awk -F, -v dd="$1" 'NR>1{d=substr($1,1,10); if(d==dd)c++} END{print c+0}'; }
D1_DAILY="$(printf '%s\n' "$DAILY2" | get_daily "$D1")"
D2_DAILY="$(printf '%s\n' "$DAILY2" | get_daily "$D2")"
D1_RAW="$(printf '%s\n' "$RAW2" | raw_count "$D1")"
D2_RAW="$(printf '%s\n' "$RAW2" | raw_count "$D2")"

[ "$D1_DAILY" = "$D1_RAW" ] && [ "$D2_DAILY" = "$D2_RAW" ] && \
[ $((D1_DAILY - D1_BEFORE)) -ge 3 ] && [ $((D2_DAILY - D2_BEFORE)) -ge 3 ] && { echo "TC-4 RESULT=PASS"; exit 0; }

echo "TC-4 RESULT=FAIL"
echo "$D1 raw=$D1_RAW daily=$D1_DAILY (before=$D1_BEFORE)"
echo "$D2 raw=$D2_RAW daily=$D2_DAILY (before=$D2_BEFORE)"
exit 1
