#!/bin/sh
set -eu

PKG="com.nick.myrecoverytracker"
RAW="files/notification_log.csv"
DAILY="files/daily_notification_engagement.csv"
LOCK_HEAD="app/locks/daily_notif_engagement.head"
LOCK_HEADER="app/locks/daily_notif_engagement.header"
RCV="$PKG/.TriggerReceiver"
ACT="$PKG.ACTION_RUN_NOTIFICATION_ENGAGEMENT_ROLLUP"
OUT="evidence/v6.0/notification_engagement/tc1.18.txt"
mkdir -p "$(dirname "$OUT")"

fail(){ echo "TC1 RESULT=FAIL $1" | tee "$OUT"; exit 1; }

adb get-state >/dev/null 2>&1 || fail "(no device)"
adb shell pm path "$PKG" >/dev/null 2>&1 || fail "(app not installed)"

TODAY="$(adb shell date +%F 2>/dev/null | tr -d '\r')"
EXP="date,feature_schema_version,delivered,opened,open_rate"

# --- Prep app sandbox: fix BOTH locks, fix DAILY header (preserve body), seed RAW ---
adb shell run-as "$PKG" sh <<'IN'
set -eu
RAW="files/notification_log.csv"
DAILY="files/daily_notification_engagement.csv"
LOCK_HEAD="app/locks/daily_notif_engagement.head"
LOCK_HEADER="app/locks/daily_notif_engagement.header"
EXP="date,feature_schema_version,delivered,opened,open_rate"
mkdir -p app/locks files

# Ensure both lock variants match expected header
printf '%s\n' "$EXP" > "$LOCK_HEAD"
printf '%s\n' "$EXP" > "$LOCK_HEADER"

# Ensure DAILY has the expected header (replace only header if wrong)
if [ -f "$DAILY" ]; then
  cur="$(head -n1 "$DAILY" 2>/dev/null | tr -d '\r' || true)"
  if [ "$cur" != "$EXP" ]; then
    tmp="$(mktemp)"
    printf '%s\n' "$EXP" > "$tmp"
    tail -n +2 "$DAILY" >> "$tmp" || true
    mv "$tmp" "$DAILY"
  fi
else
  printf '%s\n' "$EXP" > "$DAILY"
fi

# RAW must be exactly 'ts,event,notif_id'
t="$(toybox date +%F)"
echo "ts,event,notif_id" > "$RAW"
echo "$t,POSTED,tc1-a"    >> "$RAW"
echo "$t,POSTED,tc1-b"    >> "$RAW"
echo "$t,CLICKED,tc1-a"   >> "$RAW"
IN

# --- Trigger rollup and give WM time ---
adb shell logcat -c >/dev/null 2>&1 || true
adb shell cmd activity broadcast -n "$RCV" -a "$ACT" --receiver-foreground --user 0 >/dev/null 2>&1 || true
sleep 4

# --- Read result row for TODAY ---
HDR="$(adb exec-out run-as "$PKG" head -n1 "$DAILY" 2>/dev/null | tr -d '\r' || true)"
ROW="$(adb exec-out run-as "$PKG" awk -F, -v d="$TODAY" 'NR>1&&$1==d{print;exit}' "$DAILY" 2>/dev/null | tr -d '\r' || true)"

{
  echo "HEADER=$HDR"
  echo "ROW=${ROW:-<none>}"
} | tee "$OUT" >/dev/null

case "$ROW" in
"$TODAY",1,2,1,0.500000) echo "TC1 RESULT=PASS" | tee -a "$OUT" ;;
*) echo "TC1 RESULT=FAIL (row=${ROW:-<none>})" | tee -a "$OUT"; exit 1 ;;
esac
