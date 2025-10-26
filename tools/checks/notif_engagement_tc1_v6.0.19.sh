#!/bin/sh
# TC1: notification_engagement rollup produces today's row: delivered=2, opened=1, open_rate=0.5
set -eu

PKG="com.nick.myrecoverytracker"
RAW="files/notification_log.csv"
DAILY="files/daily_notification_engagement.csv"
LOCK_HEAD="app/locks/daily_notif_engagement.head"
LOCK_HEADER="app/locks/daily_notif_engagement.header"
RCV="$PKG/.TriggerReceiver"
ACT="$PKG.ACTION_RUN_NOTIFICATION_ENGAGEMENT_ROLLUP"
OUT="evidence/v6.0/notification_engagement/tc1.19.txt"
mkdir -p "$(dirname "$OUT")"

fail(){ echo "TC1 RESULT=FAIL $1" | tee "$OUT"; exit 1; }

# --- Preconditions ---
adb get-state >/dev/null 2>&1 || fail "(no device)"
adb shell pm path "$PKG" >/dev/null 2>&1 || fail "(app not installed)"

TODAY="$(adb shell date +%F | tr -d '\r')"
EXP="date,feature_schema_version,delivered,opened,open_rate"

# --- Prepare app sandbox deterministically (inside run-as) ---
# - Ensure both lock files (.head and .header) match EXP
# - Ensure DAILY header is EXP (preserve body if present)
# - Ensure schema_versions.csv has notification_engagement=1 and daily_notification_engagement=1
# - Seed RAW for today with 2 delivered/posted and 1 clicked
adb shell run-as "$PKG" sh <<'IN'
set -eu

# If the data dir is temporarily RO (post-OS update glitch), bail with a clear message
touch /data/local/tmp/.rwcheck 2>/dev/null || true

mkdir -p app/locks files

EXP="date,feature_schema_version,delivered,opened,open_rate"

# Locks (both variants, some builds read .head, others .header)
printf '%s\n' "$EXP" > app/locks/daily_notif_engagement.head
printf '%s\n' "$EXP" > app/locks/daily_notif_engagement.header

# DAILY header normalize (keep body)
DAILY="files/daily_notification_engagement.csv"
if [ -f "$DAILY" ]; then
  cur="$(head -n1 "$DAILY" 2>/dev/null | tr -d '\r' || true)"
  if [ "$cur" != "$EXP" ]; then
    tmp="$(mktemp)"; printf '%s\n' "$EXP" > "$tmp"
    tail -n +2 "$DAILY" >> "$tmp" 2>/dev/null || true
    mv "$tmp" "$DAILY"
  fi
else
  printf '%s\n' "$EXP" > "$DAILY"
fi

# schema_versions: ensure required feature versions are present
SC="files/schema_versions.csv"
HDR="ts,feature,version"
if [ -f "$SC" ]; then
  h="$(head -n1 "$SC" 2>/dev/null | tr -d '\r' || true)"
  if [ "$h" != "$HDR" ]; then
    # Replace bad header, keep body if any
    body="$(tail -n +2 "$SC" 2>/dev/null || true)"
    printf '%s\n' "$HDR" > "$SC"
    [ -n "$body" ] && printf '%s\n' "$body" >> "$SC"
  fi
else
  printf '%s\n' "$HDR" > "$SC"
fi
now="$(toybox date '+%F %T')"
tmp="$(mktemp)"
{
  echo "$HDR"
  tail -n +2 "$SC" 2>/dev/null | toybox awk -F, '
    tolower($2)!="notification_engagement" && tolower($2)!="daily_notification_engagement"{print $0}
  '
  echo "$now,notification_engagement,1"
  echo "$now,daily_notification_engagement,1"
} > "$tmp"
mv "$tmp" "$SC"

# RAW seed: use the schema the older parser expects (ts,event,notif_id)
t="$(toybox date +%F)"
{
  echo "ts,event,notif_id"
  echo "$t,POSTED,tc1-a"
  echo "$t,POSTED,tc1-b"
  echo "$t,CLICKED,tc1-a"
} > files/notification_log.csv
IN

# --- Fire receiver and give WorkManager a small window ---
adb shell logcat -c >/dev/null 2>&1 || true
adb shell cmd activity broadcast -n "$RCV" -a "$ACT" --receiver-foreground --user 0 >/dev/null 2>&1 || true

# Poll up to ~5s for the row to appear (devices vary)
i=0
ROW=""
while [ $i -lt 10 ]; do
  ROW="$(adb exec-out run-as "$PKG" awk -F, -v d="$TODAY" 'NR>1&&$1==d{print;exit}' "$DAILY" 2>/dev/null | tr -d '\r' || true)"
  [ -n "$ROW" ] && break
  sleep 0.5
  i=$((i+1))
done

# --- If missing or wrong, emit diagnostics then fail ---
if [ "$ROW" != "$TODAY,1,2,1,0.500000" ]; then
  {
    echo "HEADER=$(adb exec-out run-as "$PKG" head -n1 "$DAILY" 2>/dev/null | tr -d '\r' || true)"
    echo "ROW=${ROW:-<none>}"
    echo "--- RAW top ---"
    adb exec-out run-as "$PKG" head -n5 "$RAW" 2>/dev/null | sed -e 's/\r$//' || true
    echo "--- schema_versions ---"
    adb exec-out run-as "$PKG" cat files/schema_versions.csv 2>/dev/null | tr -d '\r' || true
    echo "--- locks (.head/.header) ---"
    adb exec-out run-as "$PKG" cat "$LOCK_HEAD" 2>/dev/null | tr -d '\r' || echo "<no .head>"
    adb exec-out run-as "$PKG" cat "$LOCK_HEADER" 2>/dev/null | tr -d '\r' || echo "<no .header>"
    echo "--- recent logs ---"
    adb shell logcat -d 2>/dev/null | grep -E "TriggerReceiver|NotificationEngagementWorker|WM-Worker" || true
  } | tee "$OUT" >/dev/null
  fail "(row=${ROW:-<none>})"
fi

echo "TC1 RESULT=PASS" | tee "$OUT"
exit 0
