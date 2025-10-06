#!/bin/sh
set -eu

PKG="com.nick.myrecoverytracker"
OUTDIR="evidence/v6.0/crash"
OUT="$OUTDIR/summary.txt"
mkdir -p "$OUTDIR"

ts() { date "+%Y-%m-%dT%H:%M:%S%z"; }

adb get-state >/dev/null 2>&1 || { echo "CRASH-DIAG RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "CRASH-DIAG RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

PID="$(adb shell pidof -s "$PKG" 2>/dev/null | tr -d '\r' || true)"

adb logcat -b crash -d > "$OUTDIR/logcat_crash.txt" 2>/dev/null || true
adb logcat -v threadtime -d > "$OUTDIR/logcat_threadtime.txt" 2>/dev/null || true
[ -n "$PID" ] && grep -F " $PID " "$OUTDIR/logcat_threadtime.txt" > "$OUTDIR/logcat_threadtime.pid.txt" 2>/dev/null || true

adb shell dumpsys activity crashes > "$OUTDIR/dumpsys_activity_crashes.txt" 2>/dev/null || true
adb shell dumpsys dropbox > "$OUTDIR/dumpsys_dropbox.txt" 2>/dev/null || true
adb shell dumpsys package "$PKG" > "$OUTDIR/dumpsys_package.txt" 2>/dev/null || true
adb shell dumpsys activity processes > "$OUTDIR/dumpsys_activity_processes.txt" 2>/dev/null || true
adb shell ls -lt /data/anr/ 2>/dev/null | head -n 50 > "$OUTDIR/anr_dir_listing.txt" || true
adb shell ls -lt /data/tombstones/ 2>/dev/null | head -n 50 > "$OUTDIR/tombstones_dir_listing.txt" || true

{
  echo "=== CRASH DIAG SUMMARY ($(ts)) ==="
  echo "PACKAGE=$PKG"
  echo "PID_NOW=${PID:-[none]}"
  echo
  echo "--- Recent fatal exceptions (logcat crash buffer) ---"
  (grep -nE 'FATAL EXCEPTION|Process:|java\.lang\.|com\.nick\.myrecoverytracker' "$OUTDIR/logcat_crash.txt" 2>/dev/null || true) | tail -n 200
  echo
  echo "--- ANR markers (logcat) ---"
  (grep -nE 'ANR in|Executing service|Broadcast of Intent' "$OUTDIR/logcat_threadtime.txt" 2>/dev/null || true) | tail -n 200
  echo
  echo "--- App-specific lines (last 1000) ---"
  (grep -nE "$PKG|MyRecoveryAssistant|MyRecoveryTracker|NotificationLatencyWorker|ForegroundSleepService" "$OUTDIR/logcat_threadtime.txt" 2>/dev/null || true) | tail -n 1000
  echo
  echo "--- WorkManager / JobScheduler hints (last 300) ---"
  (grep -nE 'WorkManager|enqueue|Started work|Finished work|JobScheduler|Scheduling' "$OUTDIR/logcat_threadtime.txt" 2>/dev/null || true) | tail -n 300
  echo
  echo "--- dumpsys activity crashes (tail) ---"
  tail -n 400 "$OUTDIR/dumpsys_activity_crashes.txt" 2>/dev/null || true
  echo
  echo "--- dropbox recent entries (tail) ---"
  tail -n 200 "$OUTDIR/dumpsys_dropbox.txt" 2>/dev/null || true
  echo
  echo "--- /data/anr listing (head) ---"
  cat "$OUTDIR/anr_dir_listing.txt" 2>/dev/null || true
  echo
  echo "--- /data/tombstones listing (head) ---"
  cat "$OUTDIR/tombstones_dir_listing.txt" 2>/dev/null || true
  echo
  echo "FILES_WRITTEN:"
  ls -1 "$OUTDIR" | sed 's/^/ - /'
  echo
  echo "CRASH-DIAG RESULT=DONE"
} | tee "$OUT" >/dev/null
