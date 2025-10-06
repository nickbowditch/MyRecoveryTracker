#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
CSV_SUM="files/daily_sleep_summary.csv"
CSV_DUR="files/daily_sleep_duration.csv"
HDR_SUM_EXP="date,sleep_time,wake_time,duration_hours"
HDR_DUR_EXP="date,duration_hours"
ACT="com.nick.myrecoverytracker.ACTION_RUN_SLEEP_ROLLUP"
RCV="$PKG/.TriggerReceiver"
OUT_DIR="evidence/v6.0/sleep"
OUT="$OUT_DIR/at1.txt"
LOG="$OUT_DIR/at1.log.txt"
mkdir -p "$OUT_DIR"

fail(){ echo "AT-1 RESULT=FAIL $1" | tee "$OUT"; exit 1; }

adb get-state >/dev/null 2>&1 || fail "(no device)"
adb shell pm path "$PKG" >/dev/null 2>&1 || fail "(app not installed)"

adb exec-out run-as "$PKG" sh -c '
set -eu
mkdir -p files
[ -f "'"$CSV_SUM"'" ] || printf "'"$HDR_SUM_EXP"'\n" >"'"$CSV_SUM"'"
[ -f "'"$CSV_DUR"'" ] || printf "'"$HDR_DUR_EXP"'\n" >"'"$CSV_DUR"'"
' >/dev/null

HS="$(adb exec-out run-as "$PKG" head -n1 "$CSV_SUM" 2>/dev/null | tr -d '\r' || true)"
HD="$(adb exec-out run-as "$PKG" head -n1 "$CSV_DUR" 2>/dev/null | tr -d '\r' || true)"
[ "$HS" = "$HDR_SUM_EXP" ] || fail "(bad summary header)"
[ "$HD" = "$HDR_DUR_EXP" ] || fail "(bad duration header)"

adb shell logcat -c >/dev/null 2>&1 || true
adb shell am broadcast -a "$ACT" -n "$RCV" -p "$PKG" >/dev/null 2>&1 || fail "(broadcast failed)"
sleep 3
adb shell logcat -d > /tmp/_sleep_log.txt 2>/dev/null || true
cat /tmp/_sleep_log.txt | tee "$LOG" >/dev/null

grep -q "TriggerReceiver.*ACTION_RUN_SLEEP_ROLLUP" "$LOG" || fail "(receiver not observed)"
grep -q "SleepRollupWorker" "$LOG" || fail "(worker not observed)"

TODAY="$(adb shell date +%F 2>/dev/null | tr -d '\r')"
ROW_D="$(adb exec-out run-as "$PKG" awk -F, -v d="$TODAY" 'NR>1&&$1==d{print;exit}' "$CSV_DUR" 2>/dev/null | tr -d '\r' || true)"
ROW_S="$(adb exec-out run-as "$PKG" awk -F, -v d="$TODAY" 'NR>1&&$1==d{print;exit}' "$CSV_SUM" 2>/dev/null | tr -d '\r' || true)"
[ -n "$ROW_D" ] || fail "(duration today row missing)"
[ -n "$ROW_S" ] || fail "(summary today row missing)"

DUR_H="$(printf '%s\n' "$ROW_D" | awk -F, '{print $2}')"
printf '%s' "$DUR_H" | grep -Eq '^[0-9]+(\.[0-9]+)?$' || fail "(duration_hours not numeric)"
awk -v x="$DUR_H" 'BEGIN{exit !(x>=0 && x<=24)}' || fail "(duration_hours out of bounds)"

echo "AT-1 RESULT=PASS" | tee "$OUT"
exit 0
