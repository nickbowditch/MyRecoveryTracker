#!/bin/bash
PKG="com.nick.myrecoverytracker"

D_TODAY="$(adb shell date +%F | tr -d '\r')"
D_YEST="$(adb shell toybox date -d "@$(( $(date +%s) - 86400 ))" +%F | tr -d '\r')"

CSV="$(adb exec-out run-as "$PKG" cat files/daily_unlocks.csv 2>/dev/null || printf "")"
C_Y_BEFORE="$(awk -F, -v d="$D_YEST"  'NR>1&&$1==d{print $2; f=1; exit} END{if(!f)print 0}' <<<"$CSV")"
C_T_BEFORE="$(awk -F, -v d="$D_TODAY" 'NR>1&&$1==d{print $2; f=1; exit} END{if(!f)print 0}' <<<"$CSV")"

adb exec-out run-as "$PKG" sh -c '
y="'"$D_YEST"'"; t="'"$D_TODAY"'"; f=files/unlock_log.csv
mkdir -p files
[ -f "$f" ] || printf "ts,event\n" > "$f"
printf "%s,UNLOCK\n%s,UNLOCK\n%s,UNLOCK\n%s,UNLOCK\n" \
"$y 23:59:57" "$y 23:59:59" "$t 00:00:01" "$t 00:00:03" >> "$f"
'

for i in 1 2; do
  adb shell am broadcast -a "$PKG".ACTION_RUN_UNLOCK_ROLLUP -n "$PKG"/.TriggerReceiver >/dev/null
  sleep 2
done

CSV2="$(adb exec-out run-as "$PKG" cat files/daily_unlocks.csv 2>/dev/null || printf "")"
C_Y_AFTER="$(awk -F, -v d="$D_YEST"  'NR>1&&$1==d{print $2; f=1; exit} END{if(!f)print 0}' <<<"$CSV2")"
C_T_AFTER="$(awk -F, -v d="$D_TODAY" 'NR>1&&$1==d{print $2; f=1; exit} END{if(!f)print 0}' <<<"$CSV2")"

DY=$((C_Y_AFTER - C_Y_BEFORE))
DT=$((C_T_AFTER - C_T_BEFORE))

if [ "$DY" -eq 2 ] && [ "$DT" -eq 2 ]; then
  echo "TC-2 RESULT=PASS"
  exit 0
else
  echo "TC-2 RESULT=FAIL"
  printf "%s\n" "$CSV2" | tail -n 12
  exit 1
fi
