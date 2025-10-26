#!/bin/sh
set -eu

PKG="com.nick.myrecoverytracker"
RCV="$PKG/.TriggerReceiver"
ACT_ENG="$PKG.ACTION_RUN_NOTIFICATION_ENGAGEMENT_ROLLUP"
ACT_ROLL="$PKG.ACTION_RUN_NOTIFICATION_ROLLUP"

RAW="files/notification_log.csv"
DAILY="files/daily_notification_engagement.csv"
LOCK="app/locks/daily_notif_engagement.header"
EXP="date,feature_schema_version,delivered,opened,open_rate"

OUTDIR="evidence/v6.0/notification_engagement/gv7_debug"
OUT="$OUTDIR/gv7.22.debug.txt"
mkdir -p "$OUTDIR"

fail(){ echo "GV7 RESULT=FAIL $1" | tee "$OUT"; exit 1; }
pass(){ echo "GV7 RESULT=PASS" | tee "$OUT"; exit 0; }

adb get-state >/dev/null 2>&1 || fail "(no device)"
adb shell pm path "$PKG" >/dev/null 2>&1 || fail "(app not installed)"

adb shell am start -n "$PKG"/.MainActivity >/dev/null 2>&1 || true
sleep 1
T="$(adb shell toybox date +%F 2>/dev/null | tr -d '\r')"

hdr(){ adb exec-out run-as "$PKG" toybox head -n1 "$DAILY" 2>/dev/null | tr -d '\r' || true; }
row_for(){ adb exec-out run-as "$PKG" awk -F, -v d="$1" 'NR>1&&$1==d{print;exit}' "$DAILY" 2>/dev/null | tr -d '\r' || true; }
snap_fs(){
  adb exec-out run-as "$PKG" sh -c '
    set -eu
    echo "== app/locks =="; ls -l app/locks 2>/dev/null || true
    echo "== files (top) =="; ls -l files 2>/dev/null | toybox sed -n "1,80p" || true
    echo "== daily head =="
    toybox head -n5 files/daily_notification_engagement.csv 2>/dev/null || true
    echo "== raw head =="
    toybox head -n10 files/notification_log.csv 2>/dev/null || true
  ' > "$OUTDIR/fs_$1.txt" 2>/dev/null || true
}

adb shell run-as "$PKG" sh <<'IN'
set -eu
mkdir -p app/locks files
echo "date,feature_schema_version,delivered,opened,open_rate" > app/locks/daily_notif_engagement.header
echo "wrong,header,please,replace" > files/daily_notification_engagement.csv
t="$(toybox date +%F)"
{
  echo "ts,event,notif_id"
  echo "$t,POSTED,gv7-a"
  echo "$t,CLICKED,gv7-a"
} > files/notification_log.csv
IN

HDR_A_BEFORE="$(hdr)"
ROW_A_BEFORE="$(row_for "$T")"
echo "PHASE A: before | hdr='$HDR_A_BEFORE' row='${ROW_A_BEFORE:-<none>}'" | tee "$OUTDIR/phaseA_before.txt"

adb shell logcat -c >/dev/null 2>&1 || true
adb shell cmd activity broadcast -n "$RCV" -a "$ACT_ENG" \
  --receiver-foreground --receiver-include-background --include-stopped-packages >/dev/null 2>&1 || true
sleep 2
adb shell logcat -d > "$OUTDIR/phaseA.log" 2>/dev/null || true

i=0; ROW_A=""
while [ $i -lt 10 ]; do
  ROW_A="$(row_for "$T")"
  [ -n "$ROW_A" ] && break
  sleep 0.5; i=$((i+1))
done

HDR_A_AFTER="$(hdr)"
echo "PHASE A: after  | hdr='$HDR_A_AFTER' row='${ROW_A:-<none>}'" | tee "$OUTDIR/phaseA_after.txt"
snap_fs "A"

if [ -n "$ROW_A" ]; then
  echo "PHASE A: wrote row with BAD header (unexpected). Capturing evidence." | tee -a "$OUT"
  adb exec-out run-as "$PKG" tar cf - shared_prefs "$DAILY" "$RAW" 2>/dev/null > "$OUTDIR/phaseA_payload.tar" || true
fi

adb shell run-as "$PKG" sh <<'IN'
set -eu
mkdir -p app/locks
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

adb shell run-as "$PKG" sh <<'IN'
set -eu
d="files/daily_notification_engagement.csv"
gold="date,feature_schema_version,delivered,opened,open_rate"
{ echo "$gold"; toybox tail -n +2 "$d" 2>/dev/null || true; } > "$d.new"
toybox mv "$d.new" "$d"
IN

HDR_B_BEFORE="$(hdr)"
ROW_B_BEFORE="$(row_for "$T")"
echo "PHASE B: before | hdr='$HDR_B_BEFORE' row='${ROW_B_BEFORE:-<none>}'" | tee "$OUTDIR/phaseB_before.txt"

adb shell logcat -c >/dev/null 2>&1 || true
adb shell cmd activity broadcast -n "$RCV" -a "$ACT_ENG" \
  --receiver-foreground --receiver-include-background --include-stopped-packages >/dev/null 2>&1 || true
sleep 1
adb shell cmd activity broadcast -n "$RCV" -a "$ACT_ROLL" \
  --receiver-foreground --receiver-include-background --include-stopped-packages >/dev/null 2>&1 || true

i=0; ROW_B=""
while [ $i -lt 12 ]; do
  ROW_B="$(row_for "$T")"
  [ -n "$ROW_B" ] && break
  sleep 0.5; i=$((i+1))
done

adb shell logcat -d > "$OUTDIR/phaseB.log" 2>/dev/null || true

HDR_B_AFTER="$(hdr)"
echo "PHASE B: after  | hdr='$HDR_B_AFTER' row='${ROW_B:-<none>}'" | tee "$OUTDIR/phaseB_after.txt"
snap_fs "B"

if [ "$HDR_B_AFTER" != "$EXP" ]; then
  fail "(header wrong after fix: '$HDR_B_AFTER')"
fi

if [ "$ROW_B" != "$T,1,1,1,1.000000" ]; then
  adb exec-out run-as "$PKG" tar cf - shared_prefs "$DAILY" "$RAW" 2>/dev/null > "$OUTDIR/phaseB_payload.tar" || true
  fail "(no exact row; got '${ROW_B:-<none>}')"
fi

pass
