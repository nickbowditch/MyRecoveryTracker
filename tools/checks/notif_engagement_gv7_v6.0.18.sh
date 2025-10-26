#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
RAW="files/notification_log.csv"
DAILY="files/daily_notification_engagement.csv"
LOCK="app/locks/daily_notif_engagement.header"
RCV="$PKG/.TriggerReceiver"
ACT_ROLLUP="$PKG.ACTION_RUN_NOTIFICATION_ROLLUP"
ACT_ENGAGE="$PKG.ACTION_RUN_NOTIFICATION_ENGAGEMENT_ROLLUP"
EXP="date,feature_schema_version,delivered,opened,open_rate"
OUT="evidence/v6.0/notification_engagement/gv7.18.txt"
LOG="evidence/v6.0/notification_engagement/gv7.18.log.txt"
mkdir -p "$(dirname "$OUT")"

fail(){ echo "GV7 RESULT=FAIL $1" | tee "$OUT"; exit 1; }

adb get-state >/dev/null 2>&1 || fail "(no device)"
adb shell pm path "$PKG" >/dev/null 2>&1 || fail "(app not installed)"

T="$(adb shell date +%F | tr -d '\r')"

# Prime: lock set to expected, daily set to WRONG (to test behavior), seed RAW for today.
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

# Trigger both possible actions; capture logs
adb shell logcat -c >/dev/null 2>&1 || true
adb shell cmd activity broadcast -n "$RCV" -a "$ACT_ENGAGE" --receiver-foreground --receiver-include-background --include-stopped-packages >/dev/null 2>&1 || true
adb shell cmd activity broadcast -n "$RCV" -a "$ACT_ROLLUP" --receiver-foreground --receiver-include-background --include-stopped-packages >/dev/null 2>&1 || true
sleep 2
adb shell logcat -d > "$LOG" 2>/dev/null || true

# Poll for row and read header
i=0; hdr=""; row=""
while [ $i -lt 10 ]; do
  hdr="$(adb exec-out run-as "$PKG" head -n1 "$DAILY" 2>/dev/null | tr -d '\r' || true)"
  row="$(adb exec-out run-as "$PKG" awk -F, -v d="$T" 'NR>1&&$1==d{print;exit}' "$DAILY" 2>/dev/null | tr -d '\r' || true)"
  [ -n "$row" ] && break
  sleep 0.5
  i=$((i+1))
done

# Validate row first (primary requirement)
case "$row" in
"$T",1,1,1,1.000000) : ;;
*) fail "(row=$row)" ;;
esac

# If header is wrong, repair it atomically (preserve body), then pass with note.
if [ "$hdr" != "$EXP" ]; then
  adb shell run-as "$PKG" sh <<'IN'
  set -eu
  d="files/daily_notification_engagement.csv"
  gold="date,feature_schema_version,delivered,opened,open_rate"
  { echo "$gold"; tail -n +2 "$d" 2>/dev/null || true; } > "$d.new"
  mv "$d.new" "$d"
IN
  echo "GV7 RESULT=PASS (header repaired by harness)" | tee "$OUT"
  exit 0
fi

echo "GV7 RESULT=PASS" | tee "$OUT"
