#!/bin/sh
set -eu

PKG="com.nick.myrecoverytracker"
OUTDIR="evidence/v6.0/crash"
OUT="$OUTDIR/snapshot.txt"
mkdir -p "$OUTDIR"

adb get-state >/dev/null 2>&1 || { echo "CRASH-SNAPSHOT RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "CRASH-SNAPSHOT RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

DEVICE_TIME="$(adb shell date 2>/dev/null | tr -d '\r' || true)"

adb logcat -b crash  -v threadtime -d >"$OUTDIR/logcat_crash.txt"   2>/dev/null || true
adb logcat -b events -v threadtime -d >"$OUTDIR/logcat_events.txt"  2>/dev/null || true
adb logcat              -v threadtime -d >"$OUTDIR/logcat_all.txt"  2>/dev/null || true

adb shell dumpsys dropbox --print data_app_crash data_app_anr system_app_crash system_app_anr system_server_crash system_server_anr >"$OUTDIR/dropbox.txt" 2>/dev/null || true
adb shell ls -lt /data/anr 2>/dev/null | head -n 50 >"$OUTDIR/anr_ls.txt" || true

F_CRASH="$(grep -nE 'FATAL EXCEPTION|Fatal signal|Process:\s*'"$PKG"'(:\S+)?' "$OUTDIR/logcat_crash.txt" 2>/dev/null || true)"
F_ANR_EVT="$(grep -nE 'am_anr.*'"$PKG"'(:\S+)?' "$OUTDIR/logcat_events.txt" 2>/dev/null || true)"
F_ANR_LOG="$(grep -nE 'ANR in\s+'"${PKG//./\\.}"'(:\S+)?' "$OUTDIR/logcat_all.txt" 2>/dev/null || true)"

{
  echo "PKG=$PKG"
  echo "DEVICE_TIME=$DEVICE_TIME"
  echo
  echo "--- CRASH MATCH (logcat crash buffer) ---"
  [ -n "$F_CRASH" ] && echo "$F_CRASH" || echo "[none]"
  echo
  echo "--- ANR MATCH (events buffer) ---"
  [ -n "$F_ANR_EVT" ] && echo "$F_ANR_EVT" || echo "[none]"
  echo
  echo "--- ANR MATCH (threadtime/all) ---"
  [ -n "$F_ANR_LOG" ] && echo "$F_ANR_LOG" || echo "[none]"
  echo
  echo "--- LAST 100 LINES: crash buffer ---"
  tail -n 100 "$OUTDIR/logcat_crash.txt" 2>/dev/null || true
  echo
  echo "FILES:"
  ls -1 "$OUTDIR" | sed 's/^/ - /'
} | tee "$OUT" >/dev/null

if [ -n "$F_CRASH$F_ANR_EVT$F_ANR_LOG" ]; then
  echo "CRASH-SNAPSHOT RESULT=FOUND" | tee -a "$OUT"
  exit 1
else
  echo "CRASH-SNAPSHOT RESULT=NONE" | tee -a "$OUT"
  exit 0
fi
