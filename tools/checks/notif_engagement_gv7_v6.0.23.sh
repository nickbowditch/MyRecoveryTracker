#!/bin/sh
# Minimal, no guesses. Phase A (bad header -> no row). Phase B (fixed header -> exact row).
set -eu

PKG="com.nick.myrecoverytracker"
RCV="$PKG/.TriggerReceiver"
ACT_ROLL="com.nick.myrecoverytracker.ACTION_RUN_NOTIFICATION_ROLLUP"
ACT_ENG="com.nick.myrecoverytracker.ACTION_RUN_NOTIFICATION_ENGAGEMENT_ROLLUP"

RAW="files/notification_log.csv"
DAILY="files/daily_notification_engagement.csv"
LOCK="app/locks/daily_notif_engagement.header"
EXP="date,feature_schema_version,delivered,opened,open_rate"

OUTDIR="evidence/v6.0/notification_engagement"
OUT="$OUTDIR/gv7.23.txt"
mkdir -p "$OUTDIR"

fail(){ echo "GV7 RESULT=FAIL $1" | tee "$OUT"; exit 1; }
pass(){ echo "GV7 RESULT=PASS" | tee "$OUT"; exit 0; }

# Preconditions
adb get-state >/dev/null 2>&1 || fail "(no device)"
adb shell pm path "$PKG" >/dev/null 2>&1 || fail "(app not installed)"

# Foreground once to ensure process is warm
adb shell am start -n "$PKG"/.MainActivity >/dev/null 2>&1 || true
sleep 1

# Stable "today" from device
T="$(adb shell toybox date +%F 2>/dev/null | tr -d '\r')"

# ---------------- Phase A: WRONG header -> must NOT write a row ----------------
adb shell run-as "$PKG" sh <<'IN'
set -eu
mkdir -p app/locks files
echo "date,feature_schema_version,delivered,opened,open_rate" > app/locks/daily_notif_engagement.header
echo "wrong,header,please,replace" > files/daily_notification_engagement.csv
t="$(toybox date +%F)"
echo "ts,event,notif_id" > files/notification_log.csv
echo "$t,POSTED,gv7-a"  >> files/notification_log.csv
echo "$t,CLICKED,gv7-a" >> files/notification_log.csv
IN

adb shell logcat -c >/dev/null 2>&1 || true
adb shell cmd activity broadcast -n "$RCV" -a "$ACT_ROLL" \
  --receiver-foreground --receiver-include-background --include-stopped-packages >/dev/null 2>&1 || true
sleep 2

HDR_A="$(adb exec-out run-as "$PKG" head -n1 "$DAILY" 2>/dev/null | tr -d '\r' || true)"
ROW_A="$(adb exec-out run-as "$PKG" awk -F, -v d="$T" 'NR>1&&$1==d{print;exit}' "$DAILY" 2>/dev/null | tr -d '\r' || true)"

[ "$HDR_A" != "$EXP" ] || fail "(phaseA: header unexpectedly corrected)"
[ -z "$ROW_A" ] || fail "(phaseA: row written with bad header: $ROW_A)"

# -------- Clear possible same-day sentinels (KEEP header) --------
adb shell run-as "$PKG" sh <<'IN'
set -eu
mkdir -p app/locks
# Safe glob handling
set -- app/locks/*
if [ -e "$1" ] 2>/dev/null; then
  for f in "$@"; do
    case "$f" in
      app/locks/daily_notif_engagement.header|app/locks/daily_notif_engagement.head) : ;;
      *notif*|*engage*|*rollup*|*daily*|*done*|*stamp*|*processed*) toybox rm -f "$f" || true ;;
    esac
  done
fi
IN

# ---------------- Phase B: FIX header -> must write the exact row ----------------
adb shell run-as "$PKG" sh <<'IN'
set -eu
d="files/daily_notification_engagement.csv"
gold="date,feature_schema_version,delivered,opened,open_rate"
{ echo "$gold"; toybox tail -n +2 "$d" 2>/dev/null || true; } > "$d.new"
toybox mv "$d.new" "$d"
IN

adb shell logcat -c >/dev/null 2>&1 || true
# Fire both actions to cover mapping differences
adb shell cmd activity broadcast -n "$RCV" -a "$ACT_ENG" \
  --receiver-foreground --receiver-include-background --include-stopped-packages >/dev/null 2>&1 || true
adb shell cmd activity broadcast -n "$RCV" -a "$ACT_ROLL" \
  --receiver-foreground --receiver-include-background --include-stopped-packages >/dev/null 2>&1 || true

# Poll up to ~6s for the exact row
i=0; ROW_B=""
while [ $i -lt 12 ]; do
  ROW_B="$(adb exec-out run-as "$PKG" awk -F, -v d="$T" 'NR>1&&$1==d{print;exit}' "$DAILY" 2>/dev/null | tr -d '\r' || true)"
  [ -n "$ROW_B" ] && break
  sleep 0.5
  i=$((i+1))
done

[ "$ROW_B" = "$T,1,1,1,1.000000" ] || fail "(phaseB: row=$ROW_B)"
pass
