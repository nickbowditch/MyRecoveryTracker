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

HDR_CSV="$(adb exec-out run-as "$PKG" head -n1 "$CSV" 2>/dev/null | tr -d '\r' || true)"
[ -n "$HDR_CSV" ] || fail "(missing csv)"
HDR_LOCK="$(tr -d '\r' < "$LOCK" 2>/dev/null || true)"
[ -n "$HDR_LOCK" ] || fail "(missing lock)"
[ "$HDR_CSV" = "$EXP" ] || fail "(bad csv header)"
[ "$HDR_LOCK" = "$EXP" ] || fail "(bad lock header)"

SDK="$(adb shell getprop ro.build.version.sdk 2>/dev/null | tr -d '\r')"
PKGINFO="$(adb shell dumpsys package "$PKG" 2>/dev/null || true)"

PN_GRANTED=""
echo "$PKGINFO" | awk '/requested permissions:/{f=1;next}f && /^ +android\.permission\.POST_NOTIFICATIONS: granted=/{print;exit}' | grep -q 'granted=true' && PN_GRANTED="granted"
if [ -z "$PN_GRANTED" ]; then
  AO="$(adb shell cmd appops get "$PKG" POST_NOTIFICATION 2>/dev/null | tr -d '\r' || true)"
  echo "$AO" | grep -qiE '\bmode=(allow|allow_fg|fg|default)\b' && PN_GRANTED="appops"
fi
if [ "${SDK:-0}" -ge 33 ] && [ -z "$PN_GRANTED" ]; then
  {
    echo "POST_NOTIFICATIONS: NOT GRANTED"
    echo "-- appops POST_NOTIFICATION --"
    adb shell cmd appops get "$PKG" POST_NOTIFICATION 2>/dev/null || true
  } | tee "$LOG" >/dev/null
  fail "(POST_NOTIFICATIONS not granted)"
fi

adb shell logcat -c >/dev/null 2>&1 || true
adb shell am broadcast -a com.nick.myrecoverytracker.ACTION_RUN_NOTIFICATION_ENGAGEMENT_ROLLUP -n com.nick.myrecoverytracker/.TriggerReceiver -p "$PKG" >/dev/null 2>&1 || fail "(broadcast failed)"
sleep 1
adb shell logcat -d > /tmp/_eng_log.txt 2>/dev/null || true
cat /tmp/_eng_log.txt | tee "$LOG" >/dev/null

grep -q "TriggerReceiver.*ACTION_RUN_NOTIFICATION_ENGAGEMENT_ROLLUP" "$LOG" || fail "(receiver not observed)"
grep -q "NotificationEngagementWorker" "$LOG" || fail "(worker not observed)"
grep -q "WM-WorkerWrapper.*SUCCESS.*NotificationEngagementWorker" "$LOG" || fail "(worker did not succeed)"

TODAY="$(adb shell date +%F 2>/dev/null | tr -d '\r')"
ROW="$(adb exec-out run-as "$PKG" awk -F, -v d="$TODAY" 'NR>1 && $1==d{print;exit}' "$CSV" 2>/dev/null | tr -d '\r' || true)"
[ -n "$ROW" ] || fail "(today row missing)"

DELIVERED="$(printf '%s\n' "$ROW" | awk -F, '{print $3}')"
OPENED="$(printf '%s\n' "$ROW" | awk -F, '{print $4}')"
RATE="$(printf '%s\n' "$ROW" | awk -F, '{print $5}')"

echo "$DELIVERED" | grep -Eq '^[0-9]+$' || fail "(delivered not integer)"
echo "$OPENED" | grep -Eq '^[0-9]+$' || fail "(opened not integer)"
printf '%s' "$RATE" | grep -Eq '^[0-9]+(\.[0-9]+)?$' || fail "(open_rate not numeric)"
[ "$OPENED" -le "$DELIVERED" ] || fail "(opened > delivered)"
awk -v r="$RATE" 'BEGIN{exit !(r>=0 && r<=1)}' || fail "(open_rate out of [0,1])"

echo "AT-1 RESULT=PASS" | tee "$OUT"
exit 0
