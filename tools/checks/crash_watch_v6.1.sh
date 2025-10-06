#!/bin/sh
set -eu

PKG="com.nick.myrecoverytracker"
OUTDIR="evidence/v6.0/crash"
OUT="$OUTDIR/watch.txt"
D="${DURATION:-30}"

mkdir -p "$OUTDIR"

adb get-state >/dev/null 2>&1 || { echo "CRASH-WATCH RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "CRASH-WATCH RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

adb logcat -c >/dev/null 2>&1 || true
adb logcat -b crash -c >/dev/null 2>&1 || true
adb logcat -b events -c >/dev/null 2>&1 || true
adb logcat -b system -c >/dev/null 2>&1 || true

LAUNCH="$(adb shell cmd package resolve-activity -c android.intent.category.LAUNCHER "$PKG" 2>/dev/null | awk -F' ' '/name=/ {for(i=1;i<=NF;i++) if($i ~ /^name=/){sub(/^name=/,"",$i); print $i; exit}}' | tr -d '\r')"
[ -n "$LAUNCH" ] || LAUNCH="$PKG/.MainActivity"

adb shell am start -n "$LAUNCH" >/dev/null 2>&1 || true
sleep 1

PID="$(adb shell pidof "$PKG" 2>/dev/null | tr -d '\r' || true)"
[ -n "$PID" ] || PID=""

adb logcat -b all -v threadtime -T 1s >"$OUTDIR/logcat_all.txt" 2>/dev/null &
LC_PID=$!

i=0
FOUND=0
while [ $i -lt "$D" ]; do
  sleep 1
  CR="$(adb logcat -b crash -d 2>/dev/null | grep -nE 'FATAL EXCEPTION|Process: '"$PKG"'' || true)"
  EV="$(adb logcat -b events -d 2>/dev/null | grep -nE 'am_anr.*'"$PKG"'' || true)"
  if [ -n "$CR$EV" ]; then FOUND=1; break; fi
  i=$((i+1))
done

kill "$LC_PID" >/dev/null 2>&1 || true

adb logcat -b crash  -d >"$OUTDIR/logcat_crash.txt"  2>/dev/null || true
adb logcat -b events -d >"$OUTDIR/logcat_events.txt" 2>/dev/null || true
adb logcat -b system -d >"$OUTDIR/logcat_system.txt" 2>/dev/null || true
adb logcat -v threadtime -d >"$OUTDIR/logcat_threadtime.txt" 2>/dev/null || true

adb shell dumpsys activity processes   >"$OUTDIR/dumpsys_activity_processes.txt" 2>/dev/null || true
adb shell dumpsys activity crashes     >"$OUTDIR/dumpsys_activity_crashes.txt"   2>/dev/null || true
adb shell dumpsys dropbox -p system_app_crash system_app_anr system_server_crash system_server_anr >"$OUTDIR/dumpsys_dropbox.txt" 2>/dev/null || true

if [ -n "$PID" ]; then
  echo "$PID" > "$OUTDIR/pid.txt"
  adb shell kill -3 "$PID" >/dev/null 2>&1 || true
  sleep 2
  adb logcat -d | grep -n "pid $PID" | tail -n 200 >"$OUTDIR/last_stack_for_pid.txt" 2>/dev/null || true
fi

FATAL="$(grep -nE 'FATAL EXCEPTION|Process: '"$PKG" "$OUTDIR/logcat_crash.txt" 2>/dev/null || true)"
ANR1="$(grep -nE 'ANR in '"$PKG" "$OUTDIR/logcat_threadtime.txt" 2>/dev/null || true)"
ANR2="$(grep -nE 'am_anr.*'"$PKG" "$OUTDIR/logcat_events.txt" 2>/dev/null || true)"

{
  echo "PKG=$PKG"
  echo "DURATION_S=$D"
  echo "PID=${PID:-none}"
  echo
  echo "--- FATAL MATCH ---"
  [ -n "$FATAL" ] && echo "$FATAL" || echo "[none]"
  echo
  echo "--- ANR MATCH (events) ---"
  [ -n "$ANR2" ] && echo "$ANR2" || echo "[none]"
  echo
  echo "--- ANR MATCH (threadtime) ---"
  [ -n "$ANR1" ] && echo "$ANR1" || echo "[none]"
  echo
  echo "FILES:"
  ls -1 "$OUTDIR" | sed 's/^/ - /'
} | tee "$OUT" >/dev/null

if [ -n "$FATAL$ANR1$ANR2" ] || [ "$FOUND" -eq 1 ]; then
  echo "CRASH-WATCH RESULT=FOUND" | tee -a "$OUT"
  exit 1
else
  echo "CRASH-WATCH RESULT=NONE" | tee -a "$OUT"
  exit 0
fi
