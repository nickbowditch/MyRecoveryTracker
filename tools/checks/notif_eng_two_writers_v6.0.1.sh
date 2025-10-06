#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
SRC="app/src/main/java"
OUT_DIR="evidence/v6.0/notification_engagement"
OUT="$OUT_DIR/two_writers.txt"
LOG="$OUT_DIR/two_writers.log.txt"
mkdir -p "$OUT_DIR"

dup_refs="$(grep -R --include='*.kt' -n 'NotificationRollupWorker' "$SRC" | grep -v 'NotificationRollupWorker.kt' || true)"
[ -z "$dup_refs" ] || { echo "RW1 RESULT=FAIL (NotificationRollupWorker referenced)"; printf "%s\n" "$dup_refs" >"$LOG"; exit 1; }

adb get-state >/dev/null 2>&1 || { echo "RW1 RESULT=FAIL (no device)"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "RW1 RESULT=FAIL (app not installed)"; exit 3; }

adb shell logcat -c >/dev/null 2>&1 || true
adb shell am broadcast -a "$PKG.ACTION_RUN_NOTIFICATION_ROLLUP" -n "$PKG/.TriggerReceiver" -p "$PKG" >/dev/null 2>&1 || true
sleep 1
adb shell logcat -d 2>/dev/null | grep -E 'Notification(Engagement|Rollup)Worker|WM-WorkerWrapper' | tee "$LOG" >/dev/null

grep -q 'NotificationRollupWorker' "$LOG" && { echo "RW1 RESULT=FAIL (NotificationRollupWorker ran)"; exit 1; }
grep -q 'NotificationEngagementWorker' "$LOG" || { echo "RW1 RESULT=FAIL (Engagement worker not observed)"; exit 1; }

echo "RW1 RESULT=PASS" | tee "$OUT"
