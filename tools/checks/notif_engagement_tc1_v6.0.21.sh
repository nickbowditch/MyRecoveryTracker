#!/bin/sh
set -eu

PKG="com.nick.myrecoverytracker"
RAW="files/notification_log.csv"
DAILY="files/daily_notification_engagement.csv"
LOCK_HEAD="app/locks/daily_notif_engagement.head"
LOCK_HEADER="app/locks/daily_notif_engagement.header"
RCV="$PKG/.TriggerReceiver"
ACT="$PKG.ACTION_RUN_NOTIFICATION_ENGAGEMENT_ROLLUP"
OUT="evidence/v6.0/notification_engagement/tc1.21.txt"
mkdir -p "$(dirname "$OUT")"

fail(){ echo "TC1 RESULT=FAIL $1" | tee "$OUT"; exit 1; }

adb get-state >/dev/null 2>&1 || fail "(no device)"
adb shell pm path "$PKG"  >/dev/null 2>&1 || fail "(app not installed)"

TODAY="$(adb shell date +%F | tr -d '\r')"
EXP="date,feature_schema_version,delivered,opened,open_rate"

# Prep everything INSIDE the sandbox; avoid /tmp and avoid awk.
adb shell run-as "$PKG" sh <<'IN'
set -eu
mkdir -p app/locks files

HDR="date,feature_schema_version,delivered,opened,open_rate"

# 1) Write BOTH lock variants
printf '%s\n' "$HDR" > app/locks/daily_notif_engagement.head
printf '%s\n' "$HDR" > app/locks/daily_notif_engagement.header

# 2) Normalize DAILY header (no mktemp in /tmp)
DAILY="files/daily_notification_engagement.csv"
TMP_DAILY="files/__tmp_daily.csv"
if [ -f "$DAILY" ]; then
  cur="$(head -n1 "$DAILY" 2>/dev/null | tr -d '\r' || true)"
  if [ "$cur" != "$HDR" ]; then
    { printf '%s\n' "$HDR"; tail -n +2 "$DAILY" 2>/dev/null || true; } > "$TMP_DAILY"
    mv "$TMP_DAILY" "$DAILY"
  fi
else
  printf '%s\n' "$HDR" > "$DAILY"
fi

# 3) Ensure schema_versions has notification_engagement & daily_notification_engagement @ v1
SC="files/schema_versions.csv"
SCH="ts,feature,version"
TMP_SC="files/__tmp_schema_versions.csv"

# Ensure header
if [ -f "$SC" ]; then
  h="$(head -n1 "$SC" 2>/dev/null | tr -d '\r' || true)"
  if [ "$h" != "$SCH" ]; then
    body="$(tail -n +2 "$SC" 2>/dev/null || true)"
    printf '%s\n' "$SCH" > "$SC"
    [ -n "$body" ] && printf '%s\n' "$body" >> "$SC"
  fi
else
  printf '%s\n' "$SCH" > "$SC"
fi

# Rebuild without awk: drop rows where column 2 is either of the two features
printf '%s\n' "$SCH" > "$TMP_SC"
if [ -s "$SC" ]; then
  tail -n +2 "$SC" 2>/dev/null | while IFS= read -r line; do
    feat="$(printf '%s' "$line" | toybox cut -d, -f2 2>/dev/null || true)"
    feat_lc="$(printf '%s' "$feat" | toybox tr '[:upper:]' '[:lower:]')"
    if [ "$feat_lc" != "notification_engagement" ] && [ "$feat_lc" != "daily_notification_engagement" ]; then
      printf '%s\n' "$line" >> "$TMP_SC"
    fi
  done
fi
now="$(toybox date '+%F %T')"
printf '%s\n' "$now,notification_engagement,1"       >> "$TMP_SC"
printf '%s\n' "$now,daily_notification_engagement,1" >> "$TMP_SC"
mv "$TMP_SC" "$SC"

# 4) Seed RAW in the schema your rollup was using when tests passed
t="$(toybox date +%F)"
{
  echo "ts,event,notif_id"
  echo "$t,POSTED,tc1-a"
  echo "$t,POSTED,tc1-b"
  echo "$t,CLICKED,tc1-a"
} > files/notification_log.csv
IN

# Fire and poll for up to ~5s
adb shell logcat -c >/dev/null 2>&1 || true
adb shell cmd activity broadcast -n "$RCV" -a "$ACT" --receiver-foreground --user 0 >/dev/null 2>&1 || true

ROW=""
for i in 1 2 3 4 5 6 7 8 9 10; do
  ROW="$(adb exec-out run-as "$PKG" sh -c 'cat "'"$DAILY"'" 2>/dev/null | tr -d "\r" | toybox grep -m1 "^'"$TODAY"'," || true')"
  [ -n "$ROW" ] && break
  sleep 0.5
done

if [ "$ROW" != "$TODAY,1,2,1,0.500000" ]; then
  {
    echo "HEADER=$(adb exec-out run-as "$PKG" head -n1 "$DAILY" 2>/dev/null | tr -d '\r' || true)"
    echo "ROW=${ROW:-<none>}"
    echo "--- RAW top ---"
    adb exec-out run-as "$PKG" head -n5 "$RAW" 2>/dev/null | sed -e 's/\r$//' || true
    echo "--- schema_versions ---"
    adb exec-out run-as "$PKG" cat files/schema_versions.csv 2>/dev/null | tr -d '\r' || true
    echo "--- locks ---"
    adb exec-out run-as "$PKG" cat "$LOCK_HEAD" 2>/dev/null | tr -d '\r' || echo "<no .head>"
    adb exec-out run-as "$PKG" cat "$LOCK_HEADER" 2>/dev/null | tr -d '\r' || echo "<no .header>"
    echo "--- recent logs ---"
    adb shell logcat -d 2>/dev/null | grep -E "TriggerReceiver|NotificationEngagementWorker|WM-Worker" || true
  } | tee "$OUT" >/dev/null
  fail "(row=${ROW:-<none>})"
fi

echo "TC1 RESULT=PASS" | tee "$OUT"
exit 0
