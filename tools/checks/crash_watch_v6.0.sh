#!/bin/sh
set -eu

PKG="com.nick.myrecoverytracker"
OUTDIR="evidence/v6.0/crash"
OUT="$OUTDIR/watch.txt"
D="${DURATION:-20}"

mkdir -p "$OUTDIR"

adb get-state >/dev/null 2>&1 || { echo "CRASH-WATCH RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "CRASH-WATCH RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

adb logcat -c >/dev/null 2>&1 || true
adb logcat -b crash -c >/dev/null 2>&1 || true

MAIN=".MainActivity"
COMP="$PKG/$MAIN"
if ! adb shell cmd package resolve-activity -c android.intent.category.LAUNCHER "$PKG" >/dev/null 2>&1; then
  COMP="$(adb shell cmd package resolve-activity "$PKG" 2>/dev/null | awk -F' ' '/name=/ {for(i=1;i<=NF;i++) if($i ~ /^name=/){sub(/^name=/,"",$i); print $i; exit}}' | tr -d '\r')"
  [ -n "$COMP" ] || COMP="$PKG/.MainActivity"
fi

adb shell am start -n "$COMP" >/dev/null 2>&1 || true

adb logcat -v threadtime -T 1s >"$OUTDIR/logcat_live.txt" 2>/dev/null &
LC_PID=$!

sleep "$D" || true

kill "$LC_PID" >/dev/null 2>&1 || true
adb logcat -b crash -d >"$OUTDIR/logcat_crash.txt" 2>/dev/null || true
adb logcat -v threadtime -d >"$OUTDIR/logcat_threadtime.txt" 2>/dev/null || true
adb shell dumpsys activity crashes > "$OUTDIR/dumpsys_activity_crashes.txt" 2>/dev/null || true

FATAL="$(grep -nE 'FATAL EXCEPTION|Process: '"$PKG" "$OUTDIR/logcat_crash.txt" 2>/dev/null || true)"
ANR="$(grep -nE 'ANR in '"$PKG" "$OUTDIR/logcat_threadtime.txt" 2>/dev/null || true)"

{
  echo "PKG=$PKG"
  echo "DURATION_S=$D"
  echo
  echo "--- FATAL (logcat crash) ---"
  [ -n "$FATAL" ] && echo "$FATAL" || echo "[none]"
  echo
  echo "--- ANR (logcat) ---"
  [ -n "$ANR" ] && echo "$ANR" || echo "[none]"
  echo
  echo "--- App lines (tail 200) ---"
  (grep -n "$PKG" "$OUTDIR/logcat_threadtime.txt" 2>/dev/null || true) | tail -n 200
  echo
  echo "FILES:"
  ls -1 "$OUTDIR" | sed 's/^/ - /'
} | tee "$OUT" >/dev/null

if [ -n "$FATAL$ANR" ]; then
  echo "CRASH-WATCH RESULT=FOUND" | tee -a "$OUT"
  exit 1
else
  echo "CRASH-WATCH RESULT=NONE" | tee -a "$OUT"
  exit 0
fi
