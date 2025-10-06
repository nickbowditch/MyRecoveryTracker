#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
RAW="files/usage_events.csv"
DAILY="files/daily_usage_events.csv"
RCV="$PKG/.TriggerReceiver"
ACT="$PKG.ACTION_RUN_USAGE_EVENTS_DAILY"
OUT="evidence/v6.0/usage_events/tc1.txt"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "TC1 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "TC1 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

T="$(adb shell date +%F | tr -d '\r')"

adb shell run-as "$PKG" sh <<'IN'
set -eu
raw="files/usage_events.csv"
daily="files/daily_usage_events.csv"
mkdir -p files
[ -f "$daily" ] || printf "date,event_count\n" >"$daily"

t="$(toybox date +%F)"
echo "date,time,event_type,package" > "$raw"
echo "$t,12:00:00,ACTIVITY_RESUMED,com.test.app1" >> "$raw"
echo "$t,12:05:00,ACTIVITY_PAUSED,com.test.app1"  >> "$raw"
echo "$t,12:10:00,ACTIVITY_RESUMED,com.test.app2" >> "$raw"
echo "$t,12:15:00,ACTIVITY_PAUSED,com.test.app2"  >> "$raw"
IN

deadline=$(( $(date +%s) + 25 ))
ok=1
while :; do
adb shell cmd activity broadcast -n "$RCV" -a "$ACT" --receiver-foreground --user 0 >/dev/null 2>&1 || true
sleep 1
rows="$(adb exec-out run-as "$PKG" awk -F, -v d="$T" 'NR>1&&$1==d{print}' "$DAILY" | tr -d '\r')"
nrows="$(printf "%s\n" "$rows" | sed '/^$/d' | wc -l | tr -d ' ')"
[ "$nrows" -eq 1 ] && { ok=0; break; }
[ "$(date +%s)" -ge "$deadline" ] && break
done

[ $ok -eq 0 ] && echo "TC1 RESULT=PASS" | tee "$OUT" || echo "TC1 RESULT=FAIL (rows_for_$T=${nrows:-0})" | tee "$OUT"
exit $ok
