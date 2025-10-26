#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
RAW="files/notification_log.csv"
DAILY="files/daily_notification_engagement.csv"
LOCK="app/locks/daily_notif_engagement.header"
RCV="$PKG/.TriggerReceiver"
ACT="$PKG.ACTION_RUN_NOTIFICATION_ROLLUP"
EXP="date,feature_schema_version,delivered,opened,open_rate"
OUT="evidence/v6.0/notification_engagement/gv7.20.txt"
mkdir -p "$(dirname "$OUT")"

fail(){ echo "GV7 RESULT=FAIL $1" | tee "$OUT"; exit 1; }
pass(){ echo "GV7 RESULT=PASS" | tee "$OUT"; exit 0; }

adb get-state >/dev/null 2>&1 || fail "(no device)"
adb shell pm path "$PKG" >/dev/null 2>&1 || fail "(app not installed)"

T="$(adb shell date +%F | tr -d '\r')"

# ---------- Phase A: NEGATIVE INTEGRITY (wrong header -> no write) ----------
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

adb shell cmd activity broadcast -n "$RCV" -a "$ACT" \
  --receiver-foreground --receiver-include-background --include-stopped-packages >/dev/null 2>&1 || true

# poll for up to ~5s for ANY row; header should stay wrong
i=0; hdr=""; row=""
while [ $i -lt 10 ]; do
  hdr="$(adb exec-out run-as "$PKG" head -n1 "$DAILY" 2>/dev/null | tr -d '\r' || true)"
  row="$(adb exec-out run-as "$PKG" awk -F, -v d="$T" 'NR>1 && $1==d{print;exit}' "$DAILY" 2>/dev/null | tr -d '\r' || true)"
  [ -n "$row" ] && break
  sleep 0.5; i=$((i+1))
done

# Integrity expectation: header is STILL wrong AND no row was written
[ "$hdr" != "$EXP" ] || fail "(phaseA: header unexpectedly corrected)"
[ -z "$row" ] || fail "(phaseA: row was written despite bad header: $row)"

# ---------- Phase B: POSITIVE BEHAVIOR (fix header -> exact row appears) ----------
adb shell run-as "$PKG" sh <<'IN'
set -eu
# repair header atomically; keep body (if any)
d="files/daily_notification_engagement.csv"
gold="date,feature_schema_version,delivered,opened,open_rate"
{ echo "$gold"; tail -n +2 "$d" 2>/dev/null || true; } > "$d.new"
mv "$d.new" "$d"
IN

adb shell cmd activity broadcast -n "$RCV" -a "$ACT" \
  --receiver-foreground --receiver-include-background --include-stopped-packages >/dev/null 2>&1 || true

# poll again for target row
i=0; row=""
while [ $i -lt 10 ]; do
  row="$(adb exec-out run-as "$PKG" awk -F, -v d="$T" 'NR>1 && $1==d{print;exit}' "$DAILY" 2>/dev/null | tr -d '\r' || true)"
  [ -n "$row" ] && break
  sleep 0.5; i=$((i+1))
done

[ "$row" = "$T,1,1,1,1.000000" ] || fail "(phaseB: row=$row)"
pass
