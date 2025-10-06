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

adb logcat -c >/dev/null 2>&1 || true

adb shell cmd activity broadcast -n "$RCV" -a "$ACT_USAGE"  --receiver-foreground --user 0 >/dev/null 2>&1 || fail "(broadcast usage failed)"
adb shell cmd activity broadcast -n "$RCV" -a "$ACT_SWITCH" --receiver-foreground --user 0 >/dev/null 2>&1 || fail "(broadcast switching failed)"

deadline=$(( $(date +%s) + 30 ))
ok=0
while :; do
  HDR_D="$(adb exec-out run-as "$APP" sed -n '1p' "$CSV_DAILY" 2>/dev/null | tr -d '\r' || true)"
  [ "$HDR_D" = "date,switches,entropy" ] && { ok=1; break; }
  [ "$(date +%s)" -ge "$deadline" ] && break
  sleep 1
done
[ "$ok" -eq 1 ] || fail "(daily csv header missing/invalid)"

T="$(adb shell toybox date +%F | tr -d '\r')"

REB="$(adb exec-out run-as "$APP" awk -F, -v d="$T" '
function log2(x){return log(x)/log(2)}
BEGIN{
  prev=""
  switches=0
}
NR==1{next}
$1==d && $3=="FOREGROUND"{
  pkg=$4
  if(prev=="" ){ prev=pkg }
  else if(pkg!=prev){ switches++; prev=pkg }
  freq[pkg]++
  total++
}
END{
  ent=0.0
  if(total>0){
    for(p in freq){
      pr=freq[p]/total
      ent+=-pr*log2(pr)
    }
  }
  # print raw numeric, compare with tolerance later
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
} | tee "$OUT" >/dev/null

[ -n "$REB" ] || fail "(no rebuilt metrics from usage_events)"
[ -n "$ACT" ] || fail "(no actual metrics row for today)"

RS="$(printf '%s\n' "$REB" | awk -F'[ =]' '/switches/{print $2}' || echo 0)"
AS="$(printf '%s\n' "$ACT" | awk -F'[ =]' '/switches/{print $2}' || echo 0)"
RE="$(printf '%s\n' "$REB" | awk -F'[ =]' '/entropy/{print $2}' || echo 0)"
AE="$(printf '%s\n' "$ACT" | awk -F'[ =]' '/entropy/{print $2}' || echo 0)"

case "$RS$AS$RE$AE" in
  *[!0-9.]* ) fail "(parse error in metrics)";;
esac

SDIFF=$(( RS - AS ))
[ "${SDIFF#-}" -le 1 ] || fail "(switch count mismatch: reb=$RS act=$AS)"

EDIFF="$(awk -v a="$RE" -v b="$AE" 'BEGIN{d=(a-b); if(d<0)d=-d; print d}')"
awk -v d="$EDIFF" 'BEGIN{exit (d>0.2)?1:0}' || { echo "AT2 RESULT=FAIL (entropy diff > 0.2: reb=$RE act=$AE)" | tee -a "$OUT"; exit 1; }

echo "AT2 RESULT=PASS" | tee -a "$OUT"
exit 0
