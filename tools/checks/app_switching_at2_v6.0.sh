#!/bin/sh
set -eu
APP="com.nick.myrecoverytracker"
CSV="files/daily_app_switching.csv"
RAW="files/app_switches.csv"
RCV="$APP/.TriggerReceiver"
ACT="$APP.ACTION_RUN_APP_SWITCHING_DAILY"
OUT="evidence/v6.0/app_switching/at2.txt"
mkdir -p "$(dirname "$OUT")"
fail(){ echo "AT2 RESULT=FAIL $1" | tee "$OUT"; exit 1; }

adb get-state >/dev/null 2>&1 || fail "(no device)"
adb shell pm path "$APP" >/dev/null 2>&1 || fail "(app not installed)"

HDR="$(adb exec-out run-as "$APP" sed -n '1p' "$CSV" 2>/dev/null | tr -d '\r' || true)"
[ "$HDR" = "date,switches,entropy" ] || fail "(bad daily header)"

T="$(adb shell toybox date +%F | tr -d '\r')"

adb exec-out run-as "$APP" sh <<IN
set -eu
RAW="files/app_switches.csv"
mkdir -p files
echo "timestamp,package" > "\$RAW"
toybox date -d "$T 23:59:00" +%s | awk '{print \$1 ",com.app1"}' >> "\$RAW"
toybox date -d "$T 23:59:20" +%s | awk '{print \$1 ",com.app2"}' >> "\$RAW"
toybox date -d "$T 23:59:40" +%s | awk '{print \$1 ",com.app3"}' >> "\$RAW"
toybox date -d "$T 23:59:59" +%s | awk '{print \$1 ",com.app4"}' >> "\$RAW"
IN

adb shell cmd activity broadcast -n "$RCV" -a "$ACT" --receiver-foreground --user 0 >/dev/null 2>&1 || fail "(broadcast failed)"
sleep 3

REBUILT="$(adb exec-out run-as "$APP" awk -F, '
NR==1{prev=""; next}
{
if(prev!=""){diff=$1-prev; if(diff>0) arr[NR]=diff}
prev=$1
}
END{
n=asorti(arr)
if(n==0){print "switches=0 entropy=0"; exit}
asort(arr)
p50=arr[int(n*0.5)]
print "switches=" n " entropy=" p50
}' "$RAW" 2>/dev/null | tr -d '\r' || true)"

ACTUAL="$(adb exec-out run-as "$APP" awk -F, -v d="$T" 'NR>1 && $1==d{print "switches=" $2 " entropy=" $3}' "$CSV" 2>/dev/null | tr -d '\r' || true)"

{
echo "--- REBUILT ---"
printf '%s\n' "$REBUILT"
echo "--- ACTUAL ---"
printf '%s\n' "$ACTUAL"
} | tee "$OUT" >/dev/null

RS="$(printf '%s\n' "$REBUILT" | awk -F'[ =]' '/switches/{print $2}' || echo 0)"
AS="$(printf '%s\n' "$ACTUAL" | awk -F'[ =]' '/switches/{print $2}' || echo 0)"
RE="$(printf '%s\n' "$REBUILT" | awk -F'[ =]' '/entropy/{print $2}' || echo 0)"
AE="$(printf '%s\n' "$ACTUAL" | awk -F'[ =]' '/entropy/{print $2}' || echo 0)"

SDIFF=$((RS - AS)); [ "${SDIFF#-}" -le 1 ] || fail "(switch count mismatch)"
EDIFF="$(awk -v a="$RE" -v b="$AE" 'BEGIN{d=(a-b);if(d<0)d=-d;print d}')"
awk -v d="$EDIFF" 'BEGIN{exit (d>50)?1:0}' || { echo "AT2 RESULT=FAIL (entropy diff > 50)" | tee -a "$OUT"; exit 1; }

echo "AT2 RESULT=PASS" | tee -a "$OUT"
exit 0
