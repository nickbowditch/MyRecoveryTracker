#!/bin/sh
set -eu
APP="com.nick.myrecoverytracker"
RCV="$APP/.TriggerReceiver"
ACT_SWITCH="$APP.ACTION_RUN_APP_SWITCHING_DAILY"
ACT_USAGE="$APP.ACTION_RUN_USAGE_CAPTURE"
CSV_DAILY="files/daily_app_switching.csv"
CSV_EVENTS="files/usage_events.csv"
OUT="evidence/v6.0/app_switching/at2.txt"
mkdir -p "$(dirname "$OUT")"
fail(){ echo "AT2 RESULT=FAIL $1" | tee "$OUT"; exit 1; }

adb get-state >/dev/null 2>&1 || fail "(no device)"
adb shell pm path "$APP" >/dev/null 2>&1 || fail "(app not installed)"

HDR_E="$(adb exec-out run-as "$APP" sed -n '1p' "$CSV_EVENTS" 2>/dev/null | tr -d '\r' || true)"
[ "$HDR_E" = "date,time,event_type,package" ] || fail "(bad usage_events header)"

T="$(adb shell toybox date +%F | tr -d '\r')"

adb exec-out run-as "$APP" sh -c '
set -eu
d="'"$T"'"
f="'"$CSV_DAILY"'"
mkdir -p files
if [ ! -f "$f" ]; then
echo "date,switches,entropy" > "$f"
else
head -n1 "$f" | grep -q "^date,switches,entropy$" || { echo "date,switches,entropy" >"$f"; }
tmp="$f.tmp"
{ head -n1 "$f"; tail -n +2 "$f" | awk -F, -v d="$d" "!(\$1==d)"; } > "$tmp" 2>/dev/null || true
mv "$tmp" "$f"
fi
' >/dev/null 2>&1 || true

adb logcat -c >/dev/null 2>&1 || true

adb shell cmd activity broadcast -n "$RCV" -a "$ACT_USAGE"  --receiver-foreground --user 0 >/dev/null 2>&1 || fail "(broadcast usage failed)"
adb shell cmd activity broadcast -n "$RCV" -a "$ACT_SWITCH" --receiver-foreground --user 0 >/dev/null 2>&1 || fail "(broadcast switching failed)"

deadline=$(( $(date +%s) + 35 ))
saw_success=0
while :; do
if adb logcat -d 2>/dev/null | grep -qiE 'WM-WorkerWrapper: Worker result SUCCESS.AppSwitchingDailyWorker|AppSwitchingDailyWorker: AppSwitchingDaily'; then
saw_success=1; break
fi
[ "$(date +%s)" -ge "$deadline" ] && break
sleep 1
done
[ "$saw_success" -eq 1 ] || fail "(no AppSwitchingDailyWorker SUCCESS in logs)"

HDR_D="$(adb exec-out run-as "$APP" sed -n '1p' "$CSV_DAILY" 2>/dev/null | tr -d '\r' || true)"
[ "$HDR_D" = "date,switches,entropy" ] || fail "(daily csv header missing/invalid)"

REB="$(adb exec-out run-as "$APP" awk -F, -v d="$T" '
function log2(x){return log(x)/log(2)}
BEGIN{prev=""; switches=0; total=0}
NR==1{next}
$1==d && ($3=="FOREGROUND" || $3=="MOVE_TO_FOREGROUND"){
pkg=$4
if(prev==""){ prev=pkg }
else if(pkg!=prev){ switches++; prev=pkg }
freq[pkg]++; total++
}
END{
ent=0.0
if(total>0){
for(p in freq){
pr=freq[p]/total
ent+=-prlog2(pr)
}
}
printf("switches=%d entropy=%.1f\n", switches, ent)
}
' "$CSV_EVENTS" 2>/dev/null | tr -d '\r' || true)"

ACT="$(adb exec-out run-as "$APP" awk -F, -v d="$T" '
NR==1{next}
$1==d{ printf("switches=%s entropy=%s\n",$2,$3); exit }
' "$CSV_DAILY" 2>/dev/null | tr -d '\r' || true)"

{
echo "--- REBUILT ---"
[ -n "$REB" ] && echo "$REB" || echo "[none]"
echo "--- ACTUAL ---"
[ -n "$ACT" ] && echo "$ACT" || echo "[none]"
echo
echo "--- DEBUG: HEAD usage_events ---"
adb exec-out run-as "$APP" head -n 5 "$CSV_EVENTS" 2>/dev/null | tr -d '\r' || true
echo "--- DEBUG: TAIL usage_events (today) ---"
adb exec-out run-as "$APP" awk -F, -v d="$T" 'NR==1{print;next} $1==d' "$CSV_EVENTS" 2>/dev/null | tail -n 5 | tr -d '\r' || true
echo "--- DEBUG: DAILY ROWS ---"
adb exec-out run-as "$APP" grep -n "^$T," "$CSV_DAILY" 2>/dev/null | tr -d '\r' || true
} | tee "$OUT" >/dev/null

[ -n "$REB" ] || fail "(no rebuilt metrics from usage_events)"
[ -n "$ACT" ] || fail "(no actual metrics row for today)"

RS="$(printf '%s\n' "$REB" | awk -F'[ =]' '/switches/{print $2}' || echo 0)"
AS="$(printf '%s\n' "$ACT" | awk -F'[ =]' '/switches/{print $2}' || echo 0)"
RE="$(printf '%s\n' "$REB" | awk -F'[ =]' '/entropy/{print $2}' || echo 0)"
AE="$(printf '%s\n' "$ACT" | awk -F'[ =]' '/entropy/{print $2}' || echo 0)"

echo "$RS" | grep -Eq '^[0-9]+$'      || fail "(parse error switches REB=$RS)"
echo "$AS" | grep -Eq '^[0-9]+$'      || fail "(parse error switches ACT=$AS)"
echo "$RE" | grep -Eq '^[0-9]+([.][0-9]+)?$' || fail "(parse error entropy  REB=$RE)"
echo "$AE" | grep -Eq '^[0-9]+([.][0-9]+)?$' || fail "(parse error entropy  ACT=$AE)"

SDIFF=$(( RS - AS ))
[ "${SDIFF#-}" -le 1 ] || fail "(switch count mismatch: reb=$RS act=$AS)"

EDIFF="$(awk -v a="$RE" -v b="$AE" 'BEGIN{d=(a-b); if(d<0)d=-d; print d}')"
awk -v d="$EDIFF" 'BEGIN{exit (d>0.2)?1:0}' || { echo "AT2 RESULT=FAIL (entropy diff > 0.2: reb=$RE act=$AE)" | tee -a "$OUT"; exit 1; }

echo "AT2 RESULT=PASS" | tee -a "$OUT"
exit 0
