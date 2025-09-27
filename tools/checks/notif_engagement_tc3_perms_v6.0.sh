#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/notification_engagement/tc3.perms.txt"
CSV="files/daily_notification_engagement.csv"
LOCK="app/locks/daily_notif_engagement.header"
EXP="date,feature_schema_version,delivered,opened,open_rate"

mkdir -p "$(dirname "$OUT")"
fail(){ echo "TC-3 RESULT=FAIL $1" | tee "$OUT"; exit 1; }

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
echo "-- dumpsys appops (POST_NOTIFICATION) --"
adb shell cmd appops get "$PKG" POST_NOTIFICATION 2>/dev/null || true
echo "-- dumpsys package (permission snippet) --"
echo "$PKGINFO" | sed -n '/requested permissions:/,/install permissions:/p' || true
} | tee "$OUT" >/dev/null
echo "TC-3 RESULT=FAIL (POST_NOTIFICATIONS not granted)" | tee -a "$OUT"
exit 1
fi

USES_NLS=0
adb shell cmd package query-services -a android.service.notification.NotificationListenerService "$PKG" 2>/dev/null | grep -q "$PKG/" && USES_NLS=1
if [ "$USES_NLS" -eq 1 ]; then
EN_LIST="$(adb shell settings get secure enabled_notification_listeners 2>/dev/null | tr -d '\r' || true)"
echo "$EN_LIST" | grep -q "$PKG/" || {
{
echo "NotificationListenerService: NOT ENABLED"
echo "-- enabled_notification_listeners --"
echo "$EN_LIST"
echo "-- dumpsys notification listeners --"
adb shell dumpsys notification 2>/dev/null | sed -n '/Notification Listeners/,/Notification Assistants/p' || true
} | tee "$OUT" >/dev/null
echo "TC-3 RESULT=FAIL (NLS not enabled)" | tee -a "$OUT"
exit 1
}
fi

USES_USAGE=0
echo "$PKGINFO" | awk '/requested permissions:/{f=1;next}f && /^ +android\.permission\.PACKAGE_USAGE_STATS: granted=/{print;exit}' | grep -q 'granted=' && USES_USAGE=1
if [ "$USES_USAGE" -eq 1 ]; then
UOP="$(adb shell cmd appops get "$PKG" GET_USAGE_STATS 2>/dev/null | tr -d '\r' || true)"
echo "$UOP" | grep -qi '\bmode=allow\b' || {
{
echo "GET_USAGE_STATS: NOT ALLOWED"
echo "-- appops GET_USAGE_STATS --"
echo "$UOP"
} | tee "$OUT" >/dev/null
echo "TC-3 RESULT=FAIL (Usage Stats not allowed)" | tee -a "$OUT"
exit 1
}
fi

{
echo "POST_NOTIFICATIONS=${PN_GRANTED:-ok}"
if [ "$USES_NLS" -eq 1 ]; then
echo "-- enabled_notification_listeners --"
adb shell settings get secure enabled_notification_listeners 2>/dev/null || true
fi
if [ "$USES_USAGE" -eq 1 ]; then
echo "-- appops GET_USAGE_STATS --"
adb shell cmd appops get "$PKG" GET_USAGE_STATS 2>/dev/null || true
fi
} | tee "$OUT" >/dev/null

echo "TC-3 RESULT=PASS" | tee -a "$OUT"
exit 0
