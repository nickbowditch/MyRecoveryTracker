#!/bin/sh
set -eu

PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/notification_engagement/ee1.2.txt"
CSV="files/daily_notification_engagement.csv"
LOCK="app/locks/daily_notif_engagement.head"
EXP_HDR="date,feature_schema_version,delivered,opened,open_rate"

mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "EE-1 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "EE-1 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

PN_MODE="$(adb shell cmd appops get "$PKG" POST_NOTIFICATION 2>/dev/null | tr -d '\r' | tail -n1 || true)"
US_MODE="$(adb shell cmd appops get "$PKG" GET_USAGE_STATS 2>/dev/null | tr -d '\r' | tail -n1 || true)"
NLS_LIST="$(adb shell cmd notification allow_listener 2>/dev/null | tr -d '\r' || true)"

PN_OK=0; echo "$PN_MODE" | grep -q 'mode=allow' && PN_OK=1; [ -z "$PN_MODE" ] && PN_OK=1
US_OK=0; echo "$US_MODE" | grep -q 'mode=allow' && US_OK=1; [ -z "$US_MODE" ] && US_OK=1

LOCK_OK=0
HDR_LOCK="$(tr -d '\r' < "$LOCK" 2>/dev/null || true)"
[ "$HDR_LOCK" = "$EXP_HDR" ] && LOCK_OK=1

{
  echo "POST_NOTIFICATION: ${PN_MODE:-[none]}"
  echo "GET_USAGE_STATS: ${US_MODE:-[none]}"
  echo "ALLOW_LISTENER:"
  [ -n "$NLS_LIST" ] && echo "$NLS_LIST" || echo "[none]"
  echo "LOCK_HEADER: ${LOCK_OK:+OK}${LOCK_OK:-MISMATCH}"
} | tee "$OUT" >/dev/null

if [ "$PN_OK" -eq 1 ] && [ "$US_OK" -eq 1 ] && [ "$LOCK_OK" -eq 1 ]; then
  echo "EE-1 RESULT=PASS" | tee -a "$OUT"; exit 0
else
  echo "EE-1 RESULT=FAIL" | tee -a "$OUT"; exit 1
fi
