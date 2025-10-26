#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
RAW="files/notification_log.csv"
DAILY="files/daily_notification_engagement.csv"
LOCK="app/locks/daily_notif_engagement.head"
RCV="$PKG/.TriggerReceiver"
ACT="$PKG.ACTION_RUN_NOTIFICATION_ENGAGEMENT_ROLLUP"
OUT="evidence/v6.0/notification_engagement/tc1.16.txt"
mkdir -p "$(dirname "$OUT")"

fail(){ echo "TC1 RESULT=FAIL $1" | tee "$OUT"; exit 1; }

adb get-state >/dev/null 2>&1 || fail "(no device)"
adb shell pm path "$PKG" >/dev/null 2>&1 || fail "(app not installed)"

# Device-local date for consistent partitioning
TODAY="$(adb shell date +%F 2>/dev/null | tr -d '\r')"

# Prepare files in app sandbox
adb shell run-as "$PKG" sh <<'IN'
set -eu
mkdir -p app/locks files
lock="app/locks/daily_notif_engagement.head"
daily="files/daily_notification_engagement.csv"
raw="files/notification_log.csv"
hdr="date,feature_schema_version,delivered,opened,open_rate"

# Ensure headers; do NOT delete existing daily (keep history)
[ -f "$lock" ] || echo "$hdr" > "$lock"
[ -f "$daily" ] || echo "$hdr" > "$daily"

# Fresh RAW for today (use header 'timestamp,event,notif_id')
echo "timestamp,event,notif_id" > "$raw"
t="$(toybox date +%F)"
# Create one delivered and one opened notification for today
echo "$t,POSTED,tc1-a"    >> "$raw"
echo "$t,DELIVERED,tc1-b" >> "$raw"
echo "$t,CLICKED,tc1-a"   >> "$raw"
IN

# Trigger rollup and allow worker time
adb shell cmd activity broadcast -n "$RCV" -a "$ACT" --receiver-foreground --user 0 >/dev/null 2>&1 || true
sleep 4

# Read today's aggregated row
ROW="$(adb exec-out run-as "$PKG" awk -F, -v d="$TODAY" 'NR>1&&$1==d{print;exit}' "$DAILY" | tr -d '\r')"

case "$ROW" in
"$TODAY",1,2,1,0.500000) echo "TC1 RESULT=PASS" | tee "$OUT" ;;
*) echo "TC1 RESULT=FAIL (row=$ROW)" | tee "$OUT"; exit 1 ;;
esac
