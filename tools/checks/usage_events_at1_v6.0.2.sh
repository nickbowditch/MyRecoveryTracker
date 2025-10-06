#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
CSV="files/daily_usage_events.csv"
RAW="files/usage_events.csv"
OUT_DIR="evidence/v6.0/usage_events"
OUT="$OUT_DIR/at1.txt"
RCV="$PKG/.TriggerReceiver"
ACT="$PKG.ACTION_RUN_USAGE_EVENTS_DAILY"
EXP="date,event_count"
mkdir -p "$OUT_DIR"

fail(){ echo "AT-1 RESULT=FAIL $1" | tee "$OUT"; exit 1; }

adb get-state >/dev/null 2>&1 || fail "(no device)"
adb shell pm path "$PKG" >/dev/null 2>&1 || fail "(app not installed)"

HDR_CSV="$(adb exec-out run-as "$PKG" sed -n '1p' "$CSV" 2>/dev/null | tr -d '\r' || true)"
[ -n "$HDR_CSV" ] || HDR_CSV=""
[ -z "$HDR_CSV" ] || printf '%s' "$HDR_CSV" | grep -q "^$EXP\$" || fail "(bad/mismatched csv header)"

PRE_HASH="$(adb exec-out run-as "$PKG" cat "$CSV" 2>/dev/null | sha1sum 2>/dev/null | awk '{print $1}' || true)"
[ -n "$PRE_HASH" ] || PRE_HASH="MISSING"

TODAY="$(adb shell toybox date +%F 2>/dev/null | tr -d '\r')"
adb shell run-as "$PKG" sh <<INP >/dev/null 2>&1 || true
set -eu
mkdir -p files
if [ ! -f "$RAW" ]; then
echo "date,time,event_type,package" > "$RAW"
echo "$TODAY,12:00:00,ACTIVITY_RESUMED,com.example.app" >> "$RAW"
fi
INP

adb shell cmd activity broadcast -n "$RCV" -a "$ACT" --receiver-foreground --user 0 >/dev/null 2>&1 || fail "(broadcast failed)"
sleep 1

ROW="$(adb exec-out run-as "$PKG" awk -F, -v d="$TODAY" 'NR>1 && $1==d{print;exit}' "$CSV" 2>/dev/null | tr -d '\r' || true)"
[ -n "$ROW" ] || fail "(today row missing)"

CNT="$(printf '%s\n' "$ROW" | awk -F, '{print $2}')"
echo "$CNT" | grep -Eq '^[0-9]+$' || fail "(event_count not integer)"
awk -v c="$CNT" 'BEGIN{exit !(c>=0 && c<=500000)}' || fail "(event_count out of bounds)"

POST_HASH="$(adb exec-out run-as "$PKG" cat "$CSV" 2>/dev/null | sha1sum 2>/dev/null | awk '{print $1}' || true)"
[ -n "$POST_HASH" ] || fail "(daily csv unreadable post-run)"

if [ "$PRE_HASH" = "MISSING" ]; then
[ -n "$POST_HASH" ] || fail "(daily not created)"
else
[ "$PRE_HASH" != "$POST_HASH" ] || fail "(daily unchanged after trigger)"
fi

echo "AT-1 RESULT=PASS" | tee "$OUT"
exit 0
