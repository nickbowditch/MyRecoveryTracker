#!/bin/sh
set -eu

PKG="com.nick.myrecoverytracker"
APPDIR_REMOTE="/data/user/0/$PKG"
OUTDIR="evidence/v6.0/notification_engagement/gv7_probe"
RAW="files/notification_log.csv"
DAILY="files/daily_notification_engagement.csv"
LOCK="app/locks/daily_notif_engagement.header"
RCV="$PKG/.TriggerReceiver"
ACT_ROLL="com.nick.myrecoverytracker.ACTION_RUN_NOTIFICATION_ROLLUP"
ACT_ENG="com.nick.myrecoverytracker.ACTION_RUN_NOTIFICATION_ENGAGEMENT_ROLLUP"
EXP="date,feature_schema_version,delivered,opened,open_rate"

mkdir -p "$OUTDIR"

fail(){ echo "GV7-PROBE: FAIL $1"; exit 1; }
note(){ echo "GV7-PROBE: $*"; }

adb get-state >/dev/null 2>&1 || fail "(no device)"
adb shell pm path "$PKG" >/dev/null 2>&1 || fail "(app not installed)"

T="$(adb shell toybox date +%F | tr -d '\r')"

# small helper: snapshot file tree (path, size, mtime, md5 if available)
snapshot_fs () {
  local tag="$1"
  adb shell run-as "$PKG" sh <<'IN' | sed 's/\r$//' > "$OUTDIR/fs_${tag}.txt"
set -eu
cd .
has_md5=0; toybox md5sum --help >/dev/null 2>&1 && has_md5=1 || true
toybox find . -type f -printf "%p|%s|" -exec toybox stat -c %Y {} \; | while IFS='|' read -r p sz mt; do
  if [ "$has_md5" -eq 1 ]; then h="$(toybox md5sum "$p" | awk '{print $1}')"; else h="-"; fi
  echo "$p|$sz|$mt|$h"
done | toybox sort
IN
}

# seed Phase A: wrong header + minimal RAW for today
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

# clean logs and run Phase A via both actions (order: ENG then ROLL)
adb shell logcat -c >/dev/null 2>&1 || true
adb shell cmd activity broadcast -n "$RCV" -a "$ACT_ENG"  --receiver-foreground --receiver-include-background --include-stopped-packages >/dev/null 2>&1 || true
adb shell cmd activity broadcast -n "$RCV" -a "$ACT_ROLL" --receiver-foreground --receiver-include-background --include-stopped-packages >/dev/null 2>&1 || true
sleep 2
adb shell logcat -d > "$OUTDIR/log_phaseA.txt" 2>/dev/null || true

# snapshot after Phase A
snapshot_fs "A"

# read header + any today row after Phase A
HDR_A="$(adb exec-out run-as "$PKG" head -n1 "$DAILY" 2>/dev/null | tr -d '\r' || true)"
ROW_A="$(adb exec-out run-as "$PKG" awk -F, -v d="$T" 'NR>1&&$1==d{print;exit}' "$DAILY" 2>/dev/null | tr -d '\r' || true)"

echo "PHASE A: header='$HDR_A' row='$ROW_A'" | tee "$OUTDIR/phaseA_state.txt"

# Clear common same-day sentinels (keep header files)
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
# also nuke a likely prefs sentinel (don’t error if absent)
for x in shared_prefs/*.xml 2>/dev/null; do
  [ -f "$x" ] || continue
  toybox sed -i -e '/notif\|engage\|rollup/{
                      /last\|done\|today\|stamp\|processed/d
                    }' "$x" || true
done
IN

# Phase B: repair header atomically, then run both actions again
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

# snapshot after Phase B
snapshot_fs "B"

# read header + today row after Phase B
HDR_B="$(adb exec-out run-as "$PKG" head -n1 "$DAILY" 2>/dev/null | tr -d '\r' || true)"
ROW_B="$(adb exec-out run-as "$PKG" awk -F, -v d="$T" 'NR>1&&$1==d{print;exit}' "$DAILY" 2>/dev/null | tr -d '\r' || true)"
echo "PHASE B: header='$HDR_B' row='$ROW_B'" | tee "$OUTDIR/phaseB_state.txt"

# local diffs
diff -u "$OUTDIR/fs_A.txt" "$OUTDIR/fs_B.txt" > "$OUTDIR/fs_diff.txt" || true

# pull prefs + CSVs for eyes-on
adb exec-out run-as "$PKG" tar cf - shared_prefs "$RAW" "$DAILY" 2>/dev/null | tar xf - -C "$OUTDIR" || true

# summary
echo "==== SUMMARY ===="           | tee "$OUTDIR/summary.txt"
echo "A hdr: $HDR_A"              | tee -a "$OUTDIR/summary.txt"
echo "A row: ${ROW_A:-<none>}"    | tee -a "$OUTDIR/summary.txt"
echo "B hdr: $HDR_B"              | tee -a "$OUTDIR/summary.txt"
echo "B row: ${ROW_B:-<none>}"    | tee -a "$OUTDIR/summary.txt"
echo "fs diff in: $OUTDIR/fs_diff.txt" | tee -a "$OUTDIR/summary.txt"
echo "logs: $OUTDIR/log_phaseA.txt, $OUTDIR/log_phaseB.txt" | tee -a "$OUTDIR/summary.txt"

# hard, objective result for GV7:
if [ "$HDR_B" = "$EXP" ] && [ "$ROW_B" = "$T,1,1,1,1.000000" ]; then
  echo "GV7-PROBE RESULT=PASS" | tee -a "$OUTDIR/summary.txt"
  exit 0
fi
echo "GV7-PROBE RESULT=FAIL" | tee -a "$OUTDIR/summary.txt"
exit 1
