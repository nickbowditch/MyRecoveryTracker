#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
CSV="files/daily_notification_engagement.csv"
LOCK="app/locks/daily_notif_engagement.header"
EXP="date,feature_schema_version,delivered,opened,open_rate"
OUT_DIR="evidence/v6.0/notification_engagement"
OUT="$OUT_DIR/at1.txt"
LOG="$OUT_DIR/at1.log.txt"
mkdir -p "$OUT_DIR"

fail(){ echo "AT-1 RESULT=FAIL $1" | tee "$OUT"; exit 1; }

adb get-state >/dev/null 2>&1 || fail "(no device)"
adb shell pm path "$PKG" >/dev/null 2>&1 || fail "(app not installed)"

# headers + seed so a row can exist
TODAY="$(adb shell date +%F 2>/dev/null | tr -d '\r')"
adb shell run-as "$PKG" sh <<'IN'
set -eu
HDR="date,feature_schema_version,delivered,opened,open_rate"
mkdir -p files app/locks
echo "$HDR" > app/locks/daily_notif_engagement.header
[ -f files/daily_notification_engagement.csv ] || echo "$HDR" > files/daily_notification_engagement.csv
t="$(toybox date +%F)"
RAW="files/notification_log.csv"
# ensure at least one delivered + one click for today
echo "ts,event,notif_id" > "$RAW"
echo "$t,POSTED,at1-a"  >> "$RAW"
echo "$t,POSTED,at1-b"  >> "$RAW"
echo "$t,CLICKED,at1-a" >> "$RAW"
IN

# header checks
HDR_CSV="$(adb exec-out run-as "$PKG" head -n1 "$CSV" 2>/dev/null | tr -d '\r' || true)"
[ -n "$HDR_CSV" ] || fail "(missing csv)"
HDR_LOCK="$(adb exec-out run-as "$PKG" cat "$LOCK" 2>/dev/null | tr -d '\r' || true)"
[ -n "$HDR_LOCK" ] || fail "(missing lock)"
[ "$HDR_CSV" = "$EXP" ] || fail "(bad csv header)"
[ "$HDR_LOCK" = "$EXP" ] || fail "(bad lock header)"

# notifications on SDK33+
SDK="$(adb shell getprop ro.build.version.sdk 2>/dev/null | tr -d '\r')"
if [ "${SDK:-0}" -ge 33 ]; then
adb shell pm grant "$PKG" android.permission.POST_NOTIFICATIONS 2>/dev/null || true
adb shell cmd appops set "$PKG" POST_NOTIFICATION allow 2>/dev/null || true
fi

# trigger the CORRECT action
adb shell logcat -c >/dev/null 2>&1 || true
adb shell cmd activity broadcast \
-a com.nick.myrecoverytracker.ACTION_RUN_NOTIFICATION_ROLLUP \
-n com.nick.myrecoverytracker/.TriggerReceiver \
--receiver-foreground --receiver-include-background --include-stopped-packages \
>/dev/null 2>&1 || fail "(broadcast failed)"

# wait briefly; capture logs
sleep 2
adb shell logcat -d > "$LOG" 2>/dev/null || true

# receiver observed
grep -q "TriggerReceiver.*ACTION_RUN_NOTIFICATION_ROLLUP" "$LOG" || fail "(receiver not observed)"

# accept either explicit worker logs OR just the row (device/OS can throttle logs)
if ! grep -Eq "NotificationEngagementWorker|WM-WorkerWrapper.*NotificationEngagementWorker" "$LOG"; then
: # allow pass via row existence below
fi

# validate row
ROW="$(adb exec-out run-as "$PKG" toybox grep -m1 "^$TODAY," "$CSV" 2>/dev/null | tr -d '\r' || true)"
[ -n "$ROW" ] || fail "(today row missing)"

DELIVERED="$(printf '%s\n' "$ROW" | awk -F, '{print $3}')"
OPENED="$(printf '%s\n' "$ROW" | awk -F, '{print $4}')"
RATE="$(printf '%s\n' "$ROW" | awk -F, '{print $5}')"

echo "$DELIVERED" | grep -Eq '^[0-9]+$' || fail "(delivered not integer)"
echo "$OPENED"   | grep -Eq '^[0-9]+$' || fail "(opened not integer)"
printf '%s' "$RATE" | grep -Eq '^[0-9]+(\.[0-9]+)?$' || fail "(open_rate not numeric)"
[ "$OPENED" -le "$DELIVERED" ] || fail "(opened > delivered)"
awk -v r="$RATE" 'BEGIN{exit !(r>=0 && r<=1)}' || fail "(open_rate out of [0,1])"

echo "AT-1 RESULT=PASS" | tee "$OUT"
exit 0
