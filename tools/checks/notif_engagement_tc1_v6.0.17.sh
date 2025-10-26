#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
RAW="files/notification_log.csv"
DAILY="files/daily_notification_engagement.csv"
# Use the canonical .header (your AT-1 uses .header)
LOCK="app/locks/daily_notif_engagement.header"
RCV="$PKG/.TriggerReceiver"
ACT="$PKG.ACTION_RUN_NOTIFICATION_ENGAGEMENT_ROLLUP"
OUT="evidence/v6.0/notification_engagement/tc1.17.txt"
mkdir -p "$(dirname "$OUT")"

fail(){ echo "TC1 RESULT=FAIL $1" | tee "$OUT"; exit 1; }

adb get-state >/dev/null 2>&1 || fail "(no device)"
adb shell pm path "$PKG" >/dev/null 2>&1 || fail "(app not installed)"

TODAY="$(adb shell date +%F 2>/dev/null | tr -d '\r')"
EXP="date,feature_schema_version,delivered,opened,open_rate"

# Force headers to the expected schema and clear DAILY body (deterministic test)
adb shell run-as "$PKG" sh <<IN
set -eu
mkdir -p app/locks files
printf '%s\n' "$EXP" > "$LOCK"
printf '%s\n' "$EXP" > "$DAILY"
# RAW must be exactly 'ts,event,notif_id' per your schema notes
echo "ts,event,notif_id" > "$RAW"
t="\$(toybox date +%F)"
echo "\$t,POSTED,tc1-a"    >> "$RAW"
echo "\$t,POSTED,tc1-b"    >> "$RAW"
echo "\$t,CLICKED,tc1-a"   >> "$RAW"
IN

# Kick the worker
adb shell cmd activity broadcast -n "$RCV" -a "$ACT" --receiver-foreground --user 0 >/dev/null 2>&1 || true
sleep 4

HDR="$(adb exec-out run-as "$PKG" head -n1 "$DAILY" 2>/dev/null | tr -d '\r' || true)"
ROW="$(adb exec-out run-as "$PKG" awk -F, -v d="$TODAY" 'NR>1&&$1==d{print;exit}' "$DAILY" 2>/dev/null | tr -d '\r' || true)"

echo "HEADER=$HDR" | tee "$OUT" >/dev/null
echo "ROW=$ROW"   | tee -a "$OUT" >/dev/null

# Expect delivered=2 opened=1, open_rate=0.5 and schema_version=1
case "$ROW" in
"$TODAY",1,2,1,0.500000) echo "TC1 RESULT=PASS" | tee -a "$OUT" ;;
*) echo "TC1 RESULT=FAIL (row=$ROW)" | tee -a "$OUT"; exit 1 ;;
esac
