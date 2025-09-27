#!/bin/sh
set -eu

APP="com.nick.myrecoverytracker"
RCV="$APP/.TriggerReceiver"
ACT="$APP.ACTION_RUN_SLEEP_ROLLUP"
SUM="files/daily_sleep_summary.csv"
DUR="files/daily_sleep_duration.csv"
OUT="evidence/v6.0/sleep/gv7.3.txt"

mkdir -p "$(dirname "$OUT")"

adb logcat -c
adb shell cmd activity broadcast -n "$RCV" -a "$ACT" --receiver-foreground --user 0 >/dev/null 2>&1 || true
sleep 2

LOG="$(adb logcat -d | grep -i -E 'TriggerReceiver|SleepRollup' || true)"
TSUM="$(adb exec-out run-as "$APP" sh -c '
f="'"$SUM"'"
if [ -f "$f" ]; then
tail -n 5 "$f" | tr -d "\r"
else
echo "[MISSING: '"$SUM"']"
fi
')"
TDUR="$(adb exec-out run-as "$APP" sh -c '
f="'"$DUR"'"
if [ -f "$f" ]; then
tail -n 5 "$f" | tr -d "\r"
else
echo "[MISSING: '"$DUR"']"
fi
')"

{
echo "=== LOG ==="
echo "$LOG"
echo
echo "=== TAIL: $SUM ==="
echo "$TSUM"
echo
echo "=== TAIL: $DUR ==="
echo "$TDUR"
} | tee "$OUT" >/dev/null

[ -n "$LOG" ] || { echo "GV7 RESULT=FAIL (no log)" | tee -a "$OUT"; exit 1; }

SUM_OK="$(printf '%s\n' "$TSUM" | awk -F',' '
function dateok(d){return d ~ /^[0-9]{4}-[0-9]{2}-[0-9]{2}$/}
function isnum(x){return x ~ /^-?[0-9]+([.][0-9]+)?$/}
{ if($0 ~ /^\[MISSING:/){print "0"; exit}
if(NF>=4 && dateok($1) && ($2=="" || $2 ~ /^[0-2][0-9]:[0-5][0-9]$/) && ($3=="" || $3 ~ /^[0-2][0-9]:[0-5][0-9]$/) && ($4=="" || isnum($4))) ok=1
}
END{print ok?1:0}
')"

DUR_OK="$(printf '%s\n' "$TDUR" | awk -F',' '
function dateok(d){return d ~ /^[0-9]{4}-[0-9]{2}-[0-9]{2}$/}
function isnum(x){return x ~ /^-?[0-9]+([.][0-9]+)?$/}
{ if($0 ~ /^\[MISSING:/){print "0"; exit}
if(NF>=2 && dateok($1) && isnum($2)) ok=1
}
END{print ok?1:0}
')"

if [ "$SUM_OK" = "1" ] && [ "$DUR_OK" = "1" ]; then
echo "GV7 RESULT=PASS" | tee -a "$OUT"
exit 0
else
echo "GV7 RESULT=FAIL (bad or missing CSV rows)" | tee -a "$OUT"
exit 1
fi
