#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
RAW="files/notification_latency_log.csv"
DAILY="files/daily_notification_latency.csv"
LOCK="app/locks/daily_notif_latency.header"
RCV="$PKG/.TriggerReceiver"
ACT="com.nick.myrecoverytracker.ACTION_RUN_NOTIFICATION_LATENCY_ROLLUP"
OUT="evidence/v6.0/notification_latency/gv7.1.txt"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "GV7 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "GV7 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

T="$(adb shell date +%F | tr -d '\r')"

adb shell run-as "$PKG" sh <<'IN'
set -eu
raw="files/notification_latency_log.csv"
daily="files/daily_notification_latency.csv"
lock="app/locks/daily_notif_latency.header"
gold="date,feature_schema_version,p50_ms,p90_ms,p99_ms,count"
mkdir -p app/locks files
echo "$gold" > "$lock"
echo "wrong,header,please,replace" > "$daily"
t="$(toybox date +%F)"
echo "notif_id,posted_ts,opened_ts,latency_ms" > "$raw"
echo "gv7a,$t 10:00:00,$t 10:01:00,60000" >> "$raw"
echo "gv7b,$t 11:00:00,$t 11:05:00,300000" >> "$raw"
echo "gv7c,$t 12:00:00,$t 12:20:00,1200000" >> "$raw"
IN

adb shell cmd activity broadcast -n "$RCV" -a "$ACT" --receiver-foreground --user 0 >/dev/null 2>&1 || true
sleep 2

hdr="$(adb exec-out run-as "$PKG" head -n1 "$DAILY" | tr -d '\r')"
row="$(adb exec-out run-as "$PKG" awk -F, -v d="$T" 'NR>1&&$1==d{print;exit}' "$DAILY" | tr -d '\r')"

[ "$hdr" = "date,feature_schema_version,p50_ms,p90_ms,p99_ms,count" ] || { echo "GV7 RESULT=FAIL (bad header: $hdr)" | tee "$OUT"; exit 1; }

case "$row" in
"$T",v6.0,,,*,3) echo "GV7 RESULT=PASS" | tee "$OUT" ;;
*) echo "GV7 RESULT=FAIL (row=$row)" | tee "$OUT"; exit 1 ;;
esac
