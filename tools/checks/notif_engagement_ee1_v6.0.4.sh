#!/bin/sh
set -eu

PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/notification_engagement/ee1.4.txt"
LOCK="app/locks/daily_notif_engagement.head"
EXP_HDR="date,feature_schema_version,delivered,opened,open_rate"

mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "EE-1 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG"  >/dev/null 2>&1 || { echo "EE-1 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

HDR_LOCK="$(tr -d '\r' < "$LOCK" 2>/dev/null || true)"
[ "$HDR_LOCK" = "$EXP_HDR" ] || { echo "EE-1 RESULT=FAIL (lock header drift)" | tee "$OUT"; exit 4; }

PN_APP_OP="$(adb shell cmd appops get "$PKG" POST_NOTIFICATION 2>/dev/null | tr -d '\r' | tail -n1 || true)"
PN_APP_OP_ALT="$(adb shell cmd appops get "$PKG" POST_NOTIFICATIONS 2>/dev/null | tr -d '\r' | tail -n1 || true)"
PN_GRANTED_RUNTIME="$(adb shell dumpsys package "$PKG" 2>/dev/null | grep -i 'android.permission.POST_NOTIFICATIONS: granted=' | tr -d '\r' || true)"
NLS_LIST="$(adb shell cmd notification allow_listener 2>/dev/null | tr -d '\r' || true)"

PN_OK=0
printf '%s\n%s\n' "$PN_APP_OP" "$PN_APP_OP_ALT" | grep -qi 'allow' && PN_OK=1
printf '%s\n' "$PN_GRANTED_RUNTIME" | grep -qi 'granted=true' && PN_OK=1
[ -z "$PN_APP_OP$PN_APP_OP_ALT$PN_GRANTED_RUNTIME" ] && PN_OK=1

{
echo "POST_NOTIFICATION appops: ${PN_APP_OP:-[none]}"
echo "POST_NOTIFICATIONS appops: ${PN_APP_OP_ALT:-[none]}"
echo "POST_NOTIFICATIONS runtime: ${PN_GRANTED_RUNTIME:-[none]}"
echo "ALLOW_LISTENER:"
[ -n "$NLS_LIST" ] && echo "$NLS_LIST" || echo "[none]"
echo "LOCK_HEADER: OK"
} | tee "$OUT" >/dev/null

if [ "$PN_OK" -eq 1 ]; then
echo "EE-1 RESULT=PASS" | tee -a "$OUT"; exit 0
else
echo "EE-1 RESULT=FAIL" | tee -a "$OUT"; exit 1
fi
