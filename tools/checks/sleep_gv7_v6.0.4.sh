#!/bin/sh
set -eu

APP="com.nick.myrecoverytracker"
RCV="$APP/.TriggerReceiver"
ACT="$APP.ACTION_RUN_SLEEP_ROLLUP"
SUM="files/daily_sleep_summary.csv"
DUR="files/daily_sleep_duration.csv"
OUT="evidence/v6.0/sleep/gv7.4.txt"

mkdir -p "$(dirname "$OUT")"

adb logcat -c >/dev/null 2>&1 || true
adb shell cmd activity broadcast -n "$RCV" -a "$ACT" --receiver-foreground --user 0 >/dev/null 2>&1 || true
sleep 3

LOG="$(adb logcat -d | grep -i -E 'TriggerReceiver|SleepRollup' || true)"

HEAD_SUM="$(adb exec-out run-as "$APP" sh -c 'head -n1 "'"$SUM"'" 2>/dev/null | tr -d "\r"' || true)"
TAIL_SUM="$(adb exec-out run-as "$APP" sh -c 'tail -n5 "'"$SUM"'" 2>/dev/null | tr -d "\r"' || true)"
HEAD_DUR="$(adb exec-out run-as "$APP" sh -c 'head -n1 "'"$DUR"'" 2>/dev/null | tr -d "\r"' || true)"
TAIL_DUR="$(adb exec-out run-as "$APP" sh -c 'tail -n5 "'"$DUR"'" 2>/dev/null | tr -d "\r"' || true)"

{
echo "=== LOG ==="
printf '%s\n' "$LOG"
echo
echo "=== HEAD/Tail: $SUM ==="
printf '%s\n' "$HEAD_SUM"
printf '%s\n' "$TAIL_SUM"
echo
echo "=== HEAD/Tail: $DUR ==="
printf '%s\n' "$HEAD_DUR"
printf '%s\n' "$TAIL_DUR"
} | tee "$OUT" >/dev/null

sum_hdr_ok=0
dur_hdr_ok=0
[ -n "$HEAD_SUM" ] && printf '%s' "$HEAD_SUM" | grep -q '^date,' && sum_hdr_ok=1 || true
[ -n "$HEAD_DUR" ] && printf '%s' "$HEAD_DUR" | grep -q '^date,' && dur_hdr_ok=1 || true

sum_row_ok="$(printf '%s\n' "$TAIL_SUM" | awk -F',' '
function isdate(d){return d ~ /^[0-9]{4}-[0-9]{2}-[0-9]{2}$/}
{ if (isdate($1)) ok=1 }
END{print ok?1:0}
')"

dur_row_ok="$(printf '%s\n' "$TAIL_DUR" | awk -F',' '
function isdate(d){return d ~ /^[0-9]{4}-[0-9]{2}-[0-9]{2}$/}
function isnum(x){return x ~ /^-?[0-9]+([.][0-9]+)?$/}
{ if (isdate($1) && isnum($2)) ok=1 }
END{print ok?1:0}
')"

if [ "$sum_hdr_ok" -eq 1 ] && [ "$dur_hdr_ok" -eq 1 ] && [ "$sum_row_ok" = "1" ] && [ "$dur_row_ok" = "1" ]; then
echo "GV7 RESULT=PASS" | tee -a "$OUT"
exit 0
else
echo "GV7 RESULT=FAIL (headers or rows invalid)" | tee -a "$OUT"
exit 1
fi
