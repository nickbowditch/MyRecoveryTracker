#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
CSV="files/daily_notification_latency.csv"
LOCK="app/locks/daily_notif_latency.header"
OUT="evidence/v6.0/notification_latency/header_check.txt"
EXP="date,feature_schema_version,p50_ms,p90_ms,p99_ms,count"
mkdir -p "$(dirname "$OUT")"

fail(){ echo "HDR RESULT=FAIL $1" | tee "$OUT"; exit 1; }

adb get-state >/dev/null 2>&1 || fail "(no device)"
adb shell pm path "$PKG" >/dev/null 2>&1 || fail "(app not installed)"

[ -f "$LOCK" ] || fail "(missing lock)"
HDR_LOCK="$(tr -d '\r' < "$LOCK")"
[ "$HDR_LOCK" = "$EXP" ] || fail "(lock header drift)"

HDR_CSV="$(adb shell run-as "$PKG" head -n1 "$CSV" 2>/dev/null | tr -d '\r' || true)"
[ -n "$HDR_CSV" ] || fail "(missing csv)"
[ "$HDR_CSV" = "$EXP" ] || fail "(csv header drift)"

{
echo "LOCK=$HDR_LOCK"
echo "CSV=$HDR_CSV"
} | tee "$OUT" >/dev/null

echo "HDR RESULT=PASS" | tee -a "$OUT"
exit 0
