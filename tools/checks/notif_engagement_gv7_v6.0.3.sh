#!/bin/sh
set -eu

APP="com.nick.myrecoverytracker"
RCV="$APP/.TriggerReceiver"
ACT="$APP.ACTION_RUN_ENGAGEMENT_ROLLUP"
CSV="files/daily_notification_engagement.csv"
OUT="evidence/v6.0/notification_engagement/gv7.3.txt"

mkdir -p "$(dirname "$OUT")"

fail(){ echo "GV7 RESULT=FAIL $1" | tee -a "$OUT"; exit 1; }

ymd_shift(){ b="$1"; s="$2"
if date -j -f "%Y-%m-%d" "$b" "+%F" >/dev/null 2>&1; then date -j -v"${s}"d -f "%Y-%m-%d" "$b" "+%F"; return; fi
if command -v gdate >/dev/null 2>&1; then gdate -d "$b $s day" +%F && return; fi
if date -d "$b $s day" +%F >/dev/null 2>&1; then date -d "$b $s day" +%F && return; fi
python3 - "$b" "$s" <<'PY'
import sys,datetime
y,m,d=map(int,sys.argv[1].split("-")); off=int(sys.argv[2])
print((datetime.date(y,m,d)+datetime.timedelta(days=off)).strftime("%Y-%m-%d"))
PY
}

TODAY="$(adb shell date +%F 2>/dev/null | tr -d '\r')"
[ -n "$TODAY" ] || fail "(device date unreadable)"
YEST="$(ymd_shift "$TODAY" -1)"

adb logcat -c >/dev/null 2>&1 || true
adb shell cmd activity broadcast -n "$RCV" -a "$ACT" --receiver-foreground --user 0 >/dev/null 2>&1 || true
sleep 3

LOG="$(adb logcat -d | grep -i -E 'TriggerReceiver|Engagement|Notification.*Rollup' || true)"
ALL="$(adb exec-out run-as "$APP" sh -c '
f="'"$CSV"'"
if [ -f "$f" ]; then
cat "$f" | tr -d "\r"
else
echo "[MISSING: '"$CSV"']"
fi
')"
TAIL="$(printf '%s\n' "$ALL" | tail -n 10)"

{
echo "=== LOG ==="
echo "$LOG"
echo
echo "=== TAIL: $CSV ==="
echo "$TAIL"
} | tee "$OUT" >/dev/null

[ -n "$LOG" ] || fail "(no log)"

OK="$(
printf '%s\n' "$ALL" | awk -F',' -v t="$TODAY" -v y="$YEST" '
function is_int(s){ return s ~ /^[0-9]+$/ }
function is_num(s){ return s ~ /^[0-9]+(\.[0-9]+)?$/ }
BEGIN{ht=0; hy=0; bad=0}
$0 ~ /^\[MISSING:/ { bad=1; exit }
NR==1 { next }
{
d=$1; fs=$2; del=$3; op=$4; rate=$5
if(d!=t && d!=y) next
if(!(is_int(del) && is_int(op))) { bad=1; exit }
if((del+0)<0 || (op+0)<0) { bad=1; exit }
if((op+0)>(del+0)) { bad=1; exit }
if(rate!=""){
if(!is_num(rate)) { bad=1; exit }
if((rate+0)<0 || (rate+0)>1) { bad=1; exit }
expected_rate=0
if((del+0)>0){ expected_rate=(op+0)/(del+0) }
diff=(rate+0)-expected_rate; if(diff<0) diff=-diff
if(diff>0.02) { bad=1; exit }
}
if(d==t) ht=1
if(d==y) hy=1
}
END{
if(!bad && (ht==1 || hy==1)) print "OK"; else print "BAD"
}'
)"

if [ "$OK" = "OK" ]; then
echo "GV7 RESULT=PASS" | tee -a "$OUT"
exit 0
fi

echo "GV7 RESULT=FAIL (bad or missing CSV rows)" | tee -a "$OUT"
exit 1
