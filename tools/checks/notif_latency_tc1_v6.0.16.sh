#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
RAW="files/notification_log.csv"
DAILY="files/daily_notification_latency.csv"
RCV="$PKG/.TriggerReceiver"
ACT="$PKG.ACTION_RUN_NOTIFICATION_LATENCY_ROLLUP"
OUT="evidence/v6.0/notification_latency/tc1.16.txt"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "TC1 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "TC1 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

T="$(adb shell date +%F | tr -d '\r')"

adb shell run-as "$PKG" sh <<'IN'
set -eu
raw="files/notification_log.csv"
daily="files/daily_notification_latency.csv"
mkdir -p files
[ -f "$daily" ] || printf "date,feature_schema_version,p50_ms,p90_ms,p99_ms,count\n" >"$daily"

t="$(toybox date +%F)"
echo "ts,event,notif_id" > "$raw"
echo "$t 12:00:00,POSTED,lat-a"   >> "$raw"
echo "$t 12:00:05,CLICKED,lat-a"  >> "$raw"
echo "$t 12:10:00,POSTED,lat-b"   >> "$raw"
echo "$t 12:10:20,CLICKED,lat-b"  >> "$raw"
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
