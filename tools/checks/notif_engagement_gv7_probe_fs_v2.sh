#!/bin/sh
set -eu

PKG="com.nick.myrecoverytracker"
OUTDIR="evidence/v6.0/notification_engagement/gv7_probe_v2"
RAW="files/notification_log.csv"
DAILY="files/daily_notification_engagement.csv"
LOCK="app/locks/daily_notif_engagement.header"
RCV="$PKG/.TriggerReceiver"
ACT_ROLL="com.nick.myrecoverytracker.ACTION_RUN_NOTIFICATION_ROLLUP"
ACT_ENG="com.nick.myrecoverytracker.ACTION_RUN_NOTIFICATION_ENGAGEMENT_ROLLUP"
EXP="date,feature_schema_version,delivered,opened,open_rate"
mkdir -p "$OUTDIR"

fail(){ echo "GV7-PROBE: FAIL $1" | tee -a "$OUTDIR/summary.txt"; exit 1; }
note(){ echo "GV7-PROBE: $*" | tee -a "$OUTDIR/summary.txt"; }

adb get-state >/dev/null 2>&1 || fail "(no device)"
adb shell pm path "$PKG" >/dev/null 2>&1 || fail "(app not installed)"

T="$(adb shell toybox date +%F | tr -d '\r')"

snapshot_fs () {
  tag="$1"
  adb shell run-as "$PKG" sh <<'IN' | sed 's/\r$//' > "$OUTDIR/fs_${tag}.txt"
set -eu
toybox find . -type f | while IFS= read -r p; do
  sz="$(toybox stat -c %s "$p" 2>/dev/null || echo 0)"
  mt="$(toybox stat -c %Y "$p" 2>/dev/null || echo 0)"
  echo "$p|$sz|$mt"
done | toybox sort
IN
}

# Phase A seed: wrong header + minimal RAW today
adb shell run-as "$PKG" sh <<'IN'
set -eu
mkdir -p app/locks files shared_prefs
gold="date,feature_schema_version,delivered,opened,open_rate"
echo "$gold" > app/locks/daily_notif_engagement.header
echo "wrong,header,please,replace" > files/daily_notification_engagement.csv
t="$(toybox date +%F)"
echo "ts,event,notif_id" > files/notification_log.csv
echo "$t,POSTED,gv7-a"  >> files/notification_log.csv
echo "$t,CLICKED,gv7-a" >> files/notification_log.csv
IN

adb shell logcat -c >/dev/null 2>&1 || true
adb shell cmd activity broadcast -n "$RCV" -a "$ACT_ENG"  --receiver-foreground --receiver-include-background --include-stopped-packages >/dev/null 2>&1 || true
adb shell cmd activity broadcast -n "$RCV" -a "$ACT_ROLL" --receiver-foreground --receiver-include-background --include-stopped-packages >/dev/null 2>&1 || true
sleep 2
adb shell logcat -d > "$OUTDIR/log_phaseA.txt" 2>/dev/null || true

snapshot_fs "A"

HDR_A="$(adb exec-out run-as "$PKG" head -n1 "$DAILY" 2>/dev/null | tr -d '\r' || true)"
ROW_A="$(adb exec-out run-as "$PKG" awk -F, -v d="$T" 'NR>1&&$1==d{print;exit}' "$DAILY" 2>/dev/null | tr -d '\r' || true)"

# Clear obvious same-day sentinels (keep header file names)
adb shell run-as "$PKG" sh <<'IN'
set -eu
mkdir -p app/locks
for f in app/locks/* 2>/dev/null; do
  [ -e "$f" ] || continue
  case "$f" in
    app/locks/daily_notif_engagement.header|app/locks/daily_notif_engagement.head) : ;;
    *notif*|*engage*|*rollup*|*daily*|*done*|*stamp*|*processed*) toybox rm -f "$f" || true ;;
  esac
done
IN

# Phase B: fix header, fire both actions
adb shell run-as "$PKG" sh <<'IN'
set -eu
d="files/daily_notification_engagement.csv"
gold="date,feature_schema_version,delivered,opened,open_rate"
{ echo "$gold"; toybox tail -n +2 "$d" 2>/dev/null || true; } > "$d.new"
toybox mv "$d.new" "$d"
IN

adb shell logcat -c >/dev/null 2>&1 || true
adb shell cmd activity broadcast -n "$RCV" -a "$ACT_ENG"  --receiver-foreground --receiver-include-background --include-stopped-packages >/dev/null 2>&1 || true
adb shell cmd activity broadcast -n "$RCV" -a "$ACT_ROLL" --receiver-foreground --receiver-include-background --include-stopped-packages >/dev/null 2>&1 || true
sleep 2
adb shell logcat -d > "$OUTDIR/log_phaseB.txt" 2>/dev/null || true

snapshot_fs "B"

HDR_B="$(adb exec-out run-as "$PKG" head -n1 "$DAILY" 2>/dev/null | tr -d '\r' || true)"
ROW_B="$(adb exec-out run-as "$PKG" awk -F, -v d="$T" 'NR>1&&$1==d{print;exit}' "$DAILY" 2>/dev/null | tr -d '\r' || true)"

diff -u "$OUTDIR/fs_A.txt" "$OUTDIR/fs_B.txt" > "$OUTDIR/fs_diff.txt" || true

# pull prefs + CSVs safely (no pipe errors)
adb exec-out run-as "$PKG" tar cf - shared_prefs "$RAW" "$DAILY" 2>/dev/null > "$OUTDIR/payload.tar" || true
tar -xf "$OUTDIR/payload.tar" -C "$OUTDIR" || true
rm -f "$OUTDIR/payload.tar" || true

{
  echo "==== SUMMARY ===="
  echo "A hdr: ${HDR_A:-<none>}"
  echo "A row: ${ROW_A:-<none>}"
  echo "B hdr: ${HDR_B:-<none>}"
  echo "B row: ${ROW_B:-<none>}"
  echo "fs diff: $OUTDIR/fs_diff.txt"
  echo "logs: $OUTDIR/log_phaseA.txt, $OUTDIR/log_phaseB.txt"
} > "$OUTDIR/summary.txt"

if [ "$HDR_B" = "$EXP" ] && [ "$ROW_B" = "$T,1,1,1,1.000000" ]; then
  echo "GV7-PROBE RESULT=PASS" >> "$OUTDIR/summary.txt"; exit 0
fi
echo "GV7-PROBE RESULT=FAIL" >> "$OUTDIR/summary.txt"; exit 1
