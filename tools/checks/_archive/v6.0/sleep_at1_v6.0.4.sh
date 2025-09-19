#!/bin/bash
PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/sleep/at1.4.txt"

adb get-state >/dev/null 2>&1 || { echo "AT-1 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "AT-1 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

ACTS="$(adb shell dumpsys package "$PKG" 2>/dev/null | tr -d $'\r' | awk '
  BEGIN{inr=0}
  /\.TriggerReceiver/ {inr=1}
  inr && /action:/ {gsub(/.*action:/,""); a[$0]=1}
  END{for(k in a) if(k ~ /SLEEP/ && k ~ /(ROLLUP|ROLL|UPDATE|SYNC)/) print k}
')"
[ -n "$ACTS" ] || ACTS="$PKG.ACTION_RUN_SLEEP_ROLLUP
$PKG.ACTION_RUN_ROLLUP_SLEEP"

D_TODAY="$(adb shell date +%F | tr -d $'\r')"

CSV_B="$(adb exec-out run-as "$PKG" cat files/daily_sleep.csv 2>/dev/null || printf "")"
base_min="$(printf '%s\n' "$CSV_B" | awk -F, -v d="$D_TODAY" 'NR>1&&$1==d{print int($4*60+0.5); f=1; exit} END{if(!f)print 0}')"

adb exec-out run-as "$PKG" sh -c '
mkdir -p files
[ -f files/sleep_log.csv ]    || printf "ts,event\n" > files/sleep_log.csv
[ -f files/sleep_events.csv ] || printf "ts,event\n" > files/sleep_events.csv
t="'"$D_TODAY"'"
printf "%s,SLEEP_START\n%s,WAKE\n" "$t 01:00:00" "$t 01:30:00" >> files/sleep_log.csv
printf "%s,SLEEP\n%s,WAKE\n"        "$t 01:00:00" "$t 01:30:00" >> files/sleep_events.csv
' >/dev/null 2>&1

printf '%s\n' "$ACTS" | while IFS= read -r a; do
  [ -n "$a" ] || continue
  adb shell am broadcast -a "$a" -n "$PKG"/.TriggerReceiver >/dev/null 2>&1
done

deadline=$(( $(date +%s) + 60 ))
delta=0
while :; do
  CSV_A="$(adb exec-out run-as "$PKG" cat files/daily_sleep.csv 2>/dev/null || printf "")"
  mins="$(printf '%s\n' "$CSV_A" | awk -F, -v d="$D_TODAY" 'NR>1&&$1==d{print int($4*60+0.5); f=1; exit} END{if(!f)print 0}')"
  delta=$(( mins - base_min ))
  [ "$delta" -ge 25 ] && break
  [ "$(date +%s)" -ge "$deadline" ] && break
  sleep 1
done

if [ "$delta" -ge 25 ]; then
  echo "AT-1 RESULT=PASS" | tee "$OUT"; exit 0
else
  echo "AT-1 RESULT=FAIL (no delta)" | tee "$OUT"; exit 1
fi
