#!/bin/sh
# GV7 probe: bad header => no write; fix header => exact row appears.
# No fancy deps; safe for zsh/bash; no event expansion traps.
set -eu

PKG="com.nick.myrecoverytracker"
RCV="$PKG/.TriggerReceiver"
ACT_ROLL="com.nick.myrecoverytracker.ACTION_RUN_NOTIFICATION_ROLLUP"
ACT_ENG="com.nick.myrecoverytracker.ACTION_RUN_NOTIFICATION_ENGAGEMENT_ROLLUP"

RAW="files/notification_log.csv"
DAILY="files/daily_notification_engagement.csv"
LOCK="app/locks/daily_notif_engagement.header"
EXP="date,feature_schema_version,delivered,opened,open_rate"

OUT="evidence/v6.0/notification_engagement/gv7.22.txt"
mkdir -p "$(dirname "$OUT")"

fail(){ echo "GV7 RESULT=FAIL $1" | tee "$OUT"; exit 1; }
pass(){ echo "GV7 RESULT=PASS" | tee "$OUT"; exit 0; }

# Preconditions
adb get-state >/dev/null 2>&1 || fail "(no device)"
adb shell pm path "$PKG" >/dev/null 2>&1 || fail "(app not installed)"

# Foreground once (pre-warm process)
adb shell am start -n "$PKG"/.MainActivity >/dev/null 2>&1 || true
sleep 1

# Stable "today"
T="$(adb shell toybox date +%F 2>/dev/null | tr -d '\r')"

# ---------------- Phase A: WRONG header -> should NOT write a row ----------------
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

# -------- Clear same-day sentinels (keep header files intact) --------
adb shell run-as "$PKG" sh <<'IN'
set -eu
mkdir -p app/locks shared_prefs
# Remove likely "done today" flags
for f in app/locks/* 2>/dev/null; do
  [ -e "$f" ] || continue
  case "$f" in
    app/locks/daily_notif_engagement.header|app/locks/daily_notif_engagement.head) : ;;
    *notif*|*engage*|*rollup*|*daily*|*done*|*stamp*|*processed*) toybox rm -f "$f" || true ;;
  esac
done
# Scrub obvious last-run keys
for x in shared_prefs/*.xml 2>/dev/null; do
  [ -f "$x" ] || continue
  toybox sed -i '/notif\|engage\|rollup/{
                    /last\|done\|today\|stamp\|processed/d
                 }' "$x" || true
done
IN

# ---------------- Phase B: FIX header -> must write exact row ----------------
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
sleep 2

ROW_B="$(adb exec-out run-as "$PKG" awk -F, -v d="$T" 'NR>1&&$1==d{print;exit}' "$DAILY" 2>/dev/null | tr -d '\r' || true)"
[ "$ROW_B" = "$T,1,1,1,1.000000" ] || fail "(phaseB: row=$ROW_B)"
pass
