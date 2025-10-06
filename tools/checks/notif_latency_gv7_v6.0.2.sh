#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
RAW="files/notification_log.csv"
DAILY="files/daily_notification_latency.csv"
LOCK="app/locks/daily_notif_latency.header"
RCV="$PKG/.TriggerReceiver"
ACT="com.nick.myrecoverytracker.ACTION_RUN_NOTIFICATION_LATENCY_ROLLUP"
OUT="evidence/v6.0/notification_latency/gv7.2.txt"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "GV7 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "GV7 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

T="$(adb shell date +%F | tr -d '\r')"

adb shell run-as "$PKG" sh <<IN
set -eu
raw="$RAW"
daily="$DAILY"
lock="$LOCK"
hdr="date,feature_schema_version,p50_ms,p90_ms,p99_ms,count"
mkdir -p app/locks files
echo "\$hdr" > "\$lock"
echo "\$hdr" > "\$daily"
echo "ts,event,notif_id" > "\$raw"
echo "$T 10:00:00,POSTED,gv7a"   >> "\$raw"
echo "$T 10:01:00,CLICKED,gv7a"  >> "\$raw"
echo "$T 11:00:00,POSTED,gv7b"   >> "\$raw"
echo "$T 11:05:00,CLICKED,gv7b"  >> "\$raw"
echo "$T 12:00:00,POSTED,gv7c"   >> "\$raw"
echo "$T 12:20:00,CLICKED,gv7c"  >> "\$raw"
IN

adb shell cmd activity broadcast -n "$RCV" -a "$ACT" --receiver-foreground --user 0 >/dev/null 2>&1 || true
sleep 2

hdr="$(adb exec-out run-as "$PKG" head -n1 "$DAILY" | tr -d '\r')"
row="$(adb exec-out run-as "$PKG" awk -F, -v d="$T" 'NR>1&&$1==d{print;exit}' "$DAILY" | tr -d '\r')"

[ "$hdr" = "date,feature_schema_version,p50_ms,p90_ms,p99_ms,count" ] || { echo "GV7 RESULT=FAIL (bad header: $hdr)" | tee "$OUT"; exit 1; }
[ -n "$row" ] || { echo "GV7 RESULT=FAIL (missing row for $T)" | tee "$OUT"; exit 1; }

p50="$(echo "$row" | awk -F, '{print $3}')"
p90="$(echo "$row" | awk -F, '{print $4}')"
p99="$(echo "$row" | awk -F, '{print $5}')"
cnt="$(echo "$row" | awk -F, '{print $6}')"

case 1 in
$((p50<=p90 && p90<=p99)) ) ;;
*) echo "GV7 RESULT=FAIL (percentile order violated)" | tee "$OUT"; exit 1 ;;
esac
[ "$cnt" -ge 0 ] 2>/dev/null || { echo "GV7 RESULT=FAIL (bad count=$cnt)" | tee "$OUT"; exit 1; }

echo "GV7 RESULT=PASS" | tee "$OUT"
exit 0
