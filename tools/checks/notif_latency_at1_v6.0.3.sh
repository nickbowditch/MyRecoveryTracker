#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
CSV="files/daily_notification_latency.csv"
LOCK="app/locks/daily_notif_latency.header"
EXP="date,feature_schema_version,p50_ms,p90_ms,p99_ms,count"
OUT_DIR="evidence/v6.0/notification_latency"
OUT="$OUT_DIR/at1.txt"
LOG="$OUT_DIR/at1.log.txt"
mkdir -p "$OUT_DIR"

fail(){ echo "AT-1 RESULT=FAIL $1" | tee "$OUT"; exit 1; }

adb get-state >/dev/null 2>&1 || fail "(no device)"
adb shell pm path "$PKG" >/dev/null 2>&1 || fail "(app not installed)"

HDR_CSV="$(adb exec-out run-as "$PKG" head -n1 "$CSV" 2>/dev/null | tr -d '\r' || true)"
[ -n "$HDR_CSV" ] || fail "(missing csv)"
HDR_LOCK="$(tr -d '\r' < "$LOCK" 2>/dev/null || true)"
[ -n "$HDR_LOCK" ] || fail "(missing lock)"
[ "$HDR_CSV" = "$EXP" ] || fail "(bad csv header)"
[ "$HDR_LOCK" = "$EXP" ] || fail "(bad lock header)"

adb shell logcat -c >/dev/null 2>&1 || true
adb shell am broadcast -a com.nick.myrecoverytracker.ACTION_RUN_NOTIFICATION_LATENCY_ROLLUP -n com.nick.myrecoverytracker/.TriggerReceiver -p "$PKG" >/dev/null 2>&1 || fail "(broadcast failed)"
sleep 1
adb shell logcat -d > /tmp/_lat_log.txt 2>/dev/null || true
cat /tmp/_lat_log.txt | tee "$LOG" >/dev/null

grep -q "TriggerReceiver.*ACTION_RUN_NOTIFICATION_LATENCY_ROLLUP" "$LOG" || fail "(receiver not observed)"
grep -q "NotificationLatencyWorker" "$LOG" || fail "(worker not observed)"
grep -q "WM-WorkerWrapper.*SUCCESS.*NotificationLatencyWorker" "$LOG" || fail "(worker did not succeed)"

TODAY="$(adb shell date +%F 2>/dev/null | tr -d '\r')"
ROW="$(adb exec-out run-as "$PKG" awk -F, -v d="$TODAY" 'NR>1 && $1==d{print;exit}' "$CSV" 2>/dev/null | tr -d '\r' || true)"
[ -n "$ROW" ] || fail "(today row missing)"

P50="$(printf '%s\n' "$ROW" | awk -F, '{print $3}')"
P90="$(printf '%s\n' "$ROW" | awk -F, '{print $4}')"
P99="$(printf '%s\n' "$ROW" | awk -F, '{print $5}')"
CNT="$(printf '%s\n' "$ROW" | awk -F, '{print $6}')"

echo "$CNT" | grep -Eq '^[0-9]+$' || fail "(count not integer)"
awk -v c="$CNT" 'BEGIN{exit !(c>=0 && c<=5000)}' || fail "(count out of bounds)"

num_or_blank(){ [ -z "$1" ] && return 0; printf '%s' "$1" | grep -Eq '^[0-9]+$'; }
num_or_blank "$P50" || fail "(p50_ms non-numeric)"
num_or_blank "$P90" || fail "(p90_ms non-numeric)"
num_or_blank "$P99" || fail "(p99_ms non-numeric)"

if [ "$CNT" -eq 0 ]; then
for v in "$P50" "$P90" "$P99"; do
[ -z "$v" ] || [ "$v" -eq 0 ] || fail "(percentile should be blank or 0 when count==0)"
done
else
for v in "$P50" "$P90" "$P99"; do
[ -n "$v" ] || fail "(percentile blank with count>0)"
awk -v x="$v" 'BEGIN{exit !(x>=0 && x<=3600000)}' || fail "(percentile out of bounds)"
done
awk -v a="$P50" -v b="$P90" -v c="$P99" 'BEGIN{exit !(a<=b && b<=c)}' || fail "(percentile order violated)"
fi

echo "AT-1 RESULT=PASS" | tee "$OUT"
exit 0
