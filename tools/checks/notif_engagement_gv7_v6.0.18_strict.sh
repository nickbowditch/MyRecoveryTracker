#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
RAW="files/notification_log.csv"
DAILY="files/daily_notification_engagement.csv"
LOCK="app/locks/daily_notif_engagement.header"
RCV="$PKG/.TriggerReceiver"
ACT="$PKG.ACTION_RUN_NOTIFICATION_ROLLUP"
EXP="date,feature_schema_version,delivered,opened,open_rate"
OUT="evidence/v6.0/notification_engagement/gv7.18.strict.txt"
mkdir -p "$(dirname "$OUT")"

fail(){ echo "GV7 RESULT=FAIL $1" | tee "$OUT"; exit 1; }

adb get-state >/dev/null 2>&1 || fail "(no device)"
adb shell pm path "$PKG" >/dev/null 2>&1 || fail "(app not installed)"

T="$(adb shell date +%F | tr -d '\r')"

# Seed: correct lock, WRONG daily header, raw with POSTED/CLICKED today
adb shell run-as "$PKG" sh <<'IN'
set -eu
raw="files/notification_log.csv"
daily="files/daily_notification_engagement.csv"
lock="app/locks/daily_notif_engagement.header"
gold="date,feature_schema_version,delivered,opened,open_rate"
mkdir -p app/locks files
echo "$gold" > "$lock"
echo "wrong,header,please,replace" > "$daily"
t="$(toybox date +%F)"
echo "ts,event,notif_id" > "$raw"
echo "$t,POSTED,gv7-a"  >> "$raw"
echo "$t,CLICKED,gv7-a" >> "$raw"
IN

# Fire the canonical action
adb shell logcat -c >/dev/null 2>&1 || true
adb shell cmd activity broadcast -n "$RCV" -a "$ACT" \
  --receiver-foreground --receiver-include-background --include-stopped-packages >/dev/null 2>&1 || true
sleep 2

hdr="$(adb exec-out run-as "$PKG" head -n1 "$DAILY" 2>/dev/null | tr -d '\r' || true)"
row="$(adb exec-out run-as "$PKG" awk -F, -v d="$T" 'NR>1&&$1==d{print;exit}' "$DAILY" 2>/dev/null | tr -d '\r' || true)"

[ "$hdr" = "$EXP" ] || fail "(bad header: $hdr)"
[ "$row" = "$T,1,1,1,1.000000" ] || fail "(row=$row)"

echo "GV7 RESULT=PASS" | tee "$OUT"
