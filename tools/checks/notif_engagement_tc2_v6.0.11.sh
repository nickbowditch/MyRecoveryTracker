#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
DAILY="files/daily_notification_engagement.csv"
RCV="$PKG/.TriggerReceiver"
ACT="$PKG.ACTION_RUN_NOTIFICATION_ENGAGEMENT_ROLLUP"
OUT="evidence/v6.0/notification_engagement/tc2.11.txt"
mkdir -p "$(dirname "$OUT")"

tools/fixtures/notif_engagement_seed_v6.0.11.sh

Y_DATE="$(adb shell toybox date -d "@$(( $(adb shell toybox date +%s)-86400 ))" +%F | tr -d '\r')"
T_DATE="$(adb shell date +%F | tr -d '\r')"

deadline=$(( $(date +%s) + 30 ))
ok=1
while :; do
  adb shell cmd activity broadcast -n "$RCV" -a "$ACT" --receiver-foreground --user 0 >/dev/null 2>&1 || true
  sleep 2
  row_y="$(adb exec-out run-as "$PKG" awk -F, -v d="$Y_DATE" 'NR>1&&$1==d{print;exit}' "$DAILY" | tr -d '\r')"
  row_t="$(adb exec-out run-as "$PKG" awk -F, -v d="$T_DATE" 'NR>1&&$1==d{print;exit}' "$DAILY" | tr -d '\r')"
  if [ -n "$row_y" ] && [ -n "$row_t" ]; then ok=0; break; fi
  [ "$(date +%s)" -ge "$deadline" ] && break
done

[ $ok -eq 0 ] && { echo "TC2 RESULT=PASS" | tee "$OUT"; exit 0; } \
              || { echo "TC2 RESULT=FAIL" | tee "$OUT"; exit 1; }
