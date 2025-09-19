#!/bin/bash
PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/sleep/at1.1.txt"
adb get-state >/dev/null 2>&1 || { echo "AT-1 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "AT-1 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }
EPOCH="$(adb shell date +%s | tr -d $'\r')"
D_TODAY="$(adb shell date +%F | tr -d $'\r')"
CSV_B="$(adb exec-out run-as "$PKG" cat files/daily_sleep.csv 2>/dev/null || printf "")"
get_min(){ awk -F, -v d="$1" 'NR>1&&$1==d{print int($4*60+0.5);f=1;exit} END{if(!f)print 0}'; }
B_MIN="$(printf '%s\n' "$CSV_B" | get_min "$D_TODAY")"
adb exec-out run-as "$PKG" sh -c '
mkdir -p files
[ -f files/sleep_log.csv ] || printf "ts,event\n" > files/sleep_log.csv
tday="'"$D_TODAY"'"
printf "%s,SLEEP_START\n" "$tday 00:00:00" >> files/sleep_log.csv
printf "%s,WAKE\n"        "$tday 00:01:00" >> files/sleep_log.csv
' >/dev/null 2>&1
adb shell am broadcast -a "$PKG".ACTION_RUN_SLEEP_ROLLUP -n "$PKG"/.TriggerReceiver >/dev/null 2>&1
sleep 2
CSV_A="$(adb exec-out run-as "$PKG" cat files/daily_sleep.csv 2>/dev/null || printf "")"
A_MIN="$(printf '%s\n' "$CSV_A" | get_min "$D_TODAY")"
DELTA=$((A_MIN - B_MIN))
if [ "$DELTA" -ge 1 ]; then echo "AT-1 RESULT=PASS" | tee "$OUT"; exit 0; else echo "AT-1 RESULT=FAIL (no delta)" | tee "$OUT"; exit 1; fi
