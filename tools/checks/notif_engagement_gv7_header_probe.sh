#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
DAILY="files/daily_notification_engagement.csv"
LOCK="app/locks/daily_notif_engagement.header"
RCV="$PKG/.TriggerReceiver"
ACT="$PKG.ACTION_RUN_NOTIFICATION_ROLLUP"
EXP="date,feature_schema_version,delivered,opened,open_rate"
OUT="evidence/v6.0/notification_engagement/gv7.header_probe.txt"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "GV7-HEADER: FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "GV7-HEADER: FAIL (app not installed)" | tee "$OUT"; exit 3; }

# Wrong header, correct lock
adb shell run-as "$PKG" sh <<'IN'
set -eu
daily="files/daily_notification_engagement.csv"
lock="app/locks/daily_notif_engagement.header"
gold="date,feature_schema_version,delivered,opened,open_rate"
mkdir -p app/locks files
echo "$gold" > "$lock"
echo "wrong,header,please,replace" > "$daily"
IN

adb shell cmd activity broadcast -n "$RCV" -a "$ACT" \
  --receiver-foreground --receiver-include-background --include-stopped-packages >/dev/null 2>&1 || true
sleep 2

hdr="$(adb exec-out run-as "$PKG" head -n1 "$DAILY" 2>/dev/null | tr -d '\r' || true)"
if [ "$hdr" = "$EXP" ]; then
  echo "GV7-HEADER: SELF-HEAL=YES" | tee "$OUT"
else
  echo "GV7-HEADER: SELF-HEAL=NO (hdr='$hdr')" | tee "$OUT"
fi
