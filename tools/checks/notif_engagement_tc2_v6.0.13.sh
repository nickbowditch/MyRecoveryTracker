#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
RAW="files/notification_log.csv"
DAILY="files/daily_notification_engagement.csv"
RCV="$PKG/.TriggerReceiver"
ACT="$PKG.ACTION_RUN_NOTIFICATION_ENGAGEMENT_ROLLUP"
OUT="evidence/v6.0/notification_engagement/tc2.13.txt"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "TC2 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG"  >/dev/null 2>&1 || { echo "TC2 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }


adb shell run-as "$PKG" sh <<'IN'
set -eu
raw="files/notification_log.csv"
csv="files/daily_notification_engagement.csv"
lock="app/locks/daily_notif_engagement.head"
hdr="date,feature_schema_version,delivered,opened,open_rate"
mkdir -p app/locks files
[ -f "$lock" ] || echo "$hdr" > "$lock"
[ -f "$csv" ]  || echo "$hdr" > "$csv"
now=$(toybox date +%s)
y=$(toybox date -d "@$((now-86400))" +%F)
t=$(toybox date -d "@$now" +%F)
ts_y="$y 23:59:00"
ts_t="$t 00:01:00"
echo "ts,event,notif_id" > "$raw"
echo "$ts_y,POSTED,tc2-y"  >> "$raw"
echo "$ts_y,CLICKED,tc2-y" >> "$raw"
echo "$ts_t,POSTED,tc2-t"  >> "$raw"
echo "$ts_t,CLICKED,tc2-t" >> "$raw"
IN

Y="$(adb shell toybox date -d "@$(( $(adb shell toybox date +%s)-86400 ))" +%F | tr -d '\r')"
T="$(adb shell date +%F | tr -d '\r')"

deadline=$(( $(date +%s) + 25 ))
ok=1
while :; do
adb shell cmd activity broadcast -n "$RCV" -a "$ACT" --receiver-foreground --user 0 >/dev/null 2>&1 || true
sleep 1
row_y="$(adb exec-out run-as "$PKG" awk -F, -v d="$Y" 'NR>1&&$1==d{print;exit}' "$DAILY" | tr -d '\r')"
row_t="$(adb exec-out run-as "$PKG" awk -F, -v d="$T" 'NR>1&&$1==d{print;exit}' "$DAILY" | tr -d '\r')"
[ -n "$row_y" ] && [ -n "$row_t" ] && { ok=0; break; }
[ "$(date +%s)" -ge "$deadline" ] && break
done

[ $ok -eq 0 ] && echo "TC2 RESULT=PASS" | tee "$OUT" || echo "TC2 RESULT=FAIL" | tee "$OUT"
exit $ok
