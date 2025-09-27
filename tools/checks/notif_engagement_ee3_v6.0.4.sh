#!/bin/sh
set -eu

PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/notification_engagement/ee3.4.txt"
RCV="$PKG/.TriggerReceiver"
ACT="$PKG.ACTION_RUN_ENGAGEMENT_ROLLUP"
CSV="files/daily_notification_engagement.csv"

mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "EE-3 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "EE-3 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

BEFORE_SHA="$(adb exec-out run-as "$PKG" sh -c '
  f="'"$CSV"'"
  [ -f "$f" ] && { tr -d "\r" < "$f" | sha1sum | awk "{print \$1}"; } || echo "NONE"
' 2>/dev/null | tr -d '\r')"

TODAY="$(adb shell date +%F 2>/dev/null | tr -d '\r' || echo "")"
YDAY="$(adb shell toybox date -d "yesterday" +%F 2>/dev/null | tr -d '\r' || echo "")"

adb logcat -c >/dev/null 2>&1 || true
adb shell dumpsys deviceidle force-idle >/dev/null 2>&1 || adb shell cmd deviceidle force-idle >/dev/null 2>&1 || true
adb shell cmd activity broadcast -n "$RCV" -a "$ACT" --receiver-foreground --user 0 >/dev/null 2>&1 || true

PASS=0
WM_OK=""
LOG_OK=""
AFTER_SHA=""

i=0
while [ $i -lt 30 ]; do
  WM_OK="$(adb shell dumpsys activity service WorkManager 2>/dev/null | awk -v p="$PKG" '
    BEGIN{IGNORECASE=1}
    ($0 ~ p) && ($0 ~ /(EngagementRollup|Engagement)/) && ($0 ~ /(SUCCEEDED|SUCCESS|COMPLETED|FINISHED)/){print; exit}
  ' || true)"

  [ -n "$WM_OK" ] && { PASS=1; break; }

  LOG_OK="$(adb logcat -d 2>/dev/null | grep -iE '(TriggerReceiver|EngagementRollup|Engagement).*(SUCCEEDED|SUCCESS|COMPLETED|FINISHED|DONE)' || true)"
  [ -n "$LOG_OK" ] && { PASS=1; break; }

  AFTER_SHA="$(adb exec-out run-as "$PKG" sh -c '
    f="'"$CSV"'"
    [ -f "$f" ] && { tr -d "\r" < "$f" | sha1sum | awk "{print \$1}"; } || echo "NONE"
  ' 2>/dev/null | tr -d '\r')"
  if [ "$BEFORE_SHA" != "NONE" ] && [ "$AFTER_SHA" != "NONE" ] && [ "$AFTER_SHA" != "$BEFORE_SHA" ]; then
    PASS=1; break
  fi

  if [ -n "$TODAY$YDAY" ]; then
    if adb exec-out run-as "$PKG" sh -c '[ -f "'"$CSV"'" ] && tail -n 10 "'"$CSV"'"' 2>/dev/null \
       | tr -d '\r' | grep -E "^($TODAY${YDAY:+|$YDAY})," >/dev/null; then
      PASS=1; break
    fi
  fi

  i=$((i+1))
  sleep 1
done

{
  echo "=== BEFORE CSV SHA1 ==="
  echo "$BEFORE_SHA"
  echo
  echo "=== AFTER CSV SHA1 ==="
  echo "${AFTER_SHA:-[unset]}"
  echo
  echo "=== WORKMANAGER SUCCESS MATCH ==="
  [ -n "$WM_OK" ] && echo "$WM_OK" || echo "[none]"
  echo
  echo "=== LOGCAT SUCCESS MATCH ==="
  if [ -n "$LOG_OK" ]; then
    echo "$LOG_OK"
  else
    adb logcat -d 2>/dev/null | grep -iE 'TriggerReceiver|Engagement|Rollup|WorkManager' || echo "[none]"
  fi
  echo
  echo "=== CSV TAIL ($CSV) ==="
  adb exec-out run-as "$PKG" sh -c '
    f="'"$CSV"'"
    [ -f "$f" ] && tail -n 10 "$f" || echo "[MISSING: '"$CSV"']"
  ' | tr -d '\r'
} | tee "$OUT" >/dev/null

if [ "$PASS" -eq 1 ]; then
  echo "EE-3 RESULT=PASS" | tee -a "$OUT"; exit 0
else
  echo "EE-3 RESULT=FAIL" | tee -a "$OUT"; exit 1
fi
