#!/bin/sh
set -eu
APP="com.nick.myrecoverytracker"
RAW="files/usage_events.csv"
DAILY="files/daily_usage_events.csv"
OUT="evidence/v6.0/usage_events/schemacheck.txt"
mkdir -p "$(dirname "$OUT")"

fail(){ echo "SCHEMA-CHK RESULT=FAIL $1" | tee "$OUT"; exit 1; }

adb get-state >/dev/null 2>&1 || fail "(no device)"
adb shell pm path "$APP" >/dev/null 2>&1 || fail "(app not installed)"

RAW_HDR="$(adb exec-out run-as "$APP" head -n1 "$RAW" 2>/dev/null | tr -d '\r' || true)"
DAILY_HDR="$(adb exec-out run-as "$APP" head -n1 "$DAILY" 2>/dev/null | tr -d '\r' || true)"

{
  echo "RAW_HEADER=${RAW_HDR:-MISSING}"
  echo "DAILY_HEADER=${DAILY_HDR:-MISSING}"
} | tee "$OUT" >/dev/null

[ "$RAW_HDR" = "date,time,event_type,package" ] || fail "(raw header mismatch)"
[ "$DAILY_HDR" = "date,event_count" ] || fail "(daily header mismatch)"

echo "SCHEMA-CHK RESULT=PASS" | tee -a "$OUT"
exit 0
