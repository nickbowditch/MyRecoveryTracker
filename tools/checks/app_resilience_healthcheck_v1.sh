#!/bin/sh
set -eu

PKG="com.nick.myrecoverytracker"
APP_MAIN="$PKG/.MainActivity"
RCV="$PKG/.TriggerReceiver"

OUTDIR="evidence/v6.0/ALL_HEALTHCHECK"
OUT="$OUTDIR/app_resilience.txt"
LOGA="$OUTDIR/app_launch.log"
LOGT="$OUTDIR/triggers.log"
LOGSVC="$OUTDIR/services.dump"
LOGJOBS="$OUTDIR/jobs.dump"
mkdir -p "$OUTDIR"
: > "$OUT"

pass(){ printf "✅ %s\n" "$1" | tee -a "$OUT"; }
fail(){ printf "❌ %s\n" "$1" | tee -a "$OUT"; }

req_device() {
  adb get-state >/dev/null 2>&1 || { fail "NO DEVICE"; exit 2; }
  adb shell pm path "$PKG" >/dev/null 2>&1 || { fail "APP NOT INSTALLED"; exit 3; }
}

reset_env() {
  adb shell am force-stop "$PKG" >/dev/null 2>&1 || true
  adb shell logcat -c >/dev/null 2>&1 || true
  adb shell svc power stayon usb >/dev/null 2>&1 || true
  adb shell am start -n "$APP_MAIN" >/dev/null 2>&1 || true
  sleep 2
}

grep_log() {
  patt="$1"; file="$2"
  toybox grep -F -- "$patt" "$file" >/dev/null 2>&1
}

req_device
reset_env

# ---- Phase A: App launch signals and services ----
adb shell logcat -d > "$LOGA" 2>/dev/null || true

if grep -q "Requested ForegroundUnlockService start" "$LOGA"; then pass "ForegroundUnlockService start logged"
else fail "ForegroundUnlockService start NOT logged"; fi

if grep -q "Requested LocationCaptureService start" "$LOGA"; then pass "LocationCaptureService start logged"
else fail "LocationCaptureService start NOT logged"; fi

if grep -q "Enqueued NotificationEngagementWorker (24h periodic)" "$LOGA"; then pass "NotificationEngagement periodic enqueued"
else fail "NotificationEngagement periodic NOT enqueued"; fi

# Heartbeat/diag are best-effort; mark as info
grep -q "RedcapDiag.log executed on launch" "$LOGA" && pass "RedcapDiag log executed" || true

# Services alive
adb shell dumpsys activity services "$PKG" > "$LOGSVC" 2>/dev/null || true
grep -q "ForegroundUnlockService" "$LOGSVC" && pass "ForegroundUnlockService alive" || fail "ForegroundUnlockService NOT alive"
grep -q "LocationCaptureService" "$LOGSVC" && pass "LocationCaptureService alive" || fail "LocationCaptureService NOT alive"

# Jobs (WorkManager via JobScheduler presence)
adb shell dumpsys jobscheduler > "$LOGJOBS" 2>/dev/null || true
JOBS_CNT="$(toybox grep -c "$PKG" "$LOGJOBS" 2>/dev/null || echo 0)"
[ "$JOBS_CNT" -ge 1 ] && pass "Jobs present in JobScheduler ($JOBS_CNT)" || fail "No jobs found in JobScheduler"

# ---- Phase B: TriggerReceiver mapping smoke tests ----
adb shell logcat -c >/dev/null 2>&1 || true
adb shell cmd activity broadcast -n "$RCV" -a "$PKG.ACTION_RUN_NOTIFICATION_ENGAGEMENT_ROLLUP" --receiver-foreground --receiver-include-background --include-stopped-packages >/dev/null 2>&1 || true
adb shell cmd activity broadcast -n "$RCV" -a "$PKG.ACTION_RUN_DISTANCE_DAILY" --receiver-foreground --receiver-include-background --include-stopped-packages >/dev/null 2>&1 || true
sleep 2
adb shell logcat -d > "$LOGT" 2>/dev/null || true

grep -q "TriggerReceiver: Enqueued NotificationEngagement (once-NotificationEngagementRollup)" "$LOGT" \
  && pass "Trigger ACTION_RUN_NOTIFICATION_ENGAGEMENT_ROLLUP enqueued worker" \
  || fail "Trigger ACTION_RUN_NOTIFICATION_ENGAGEMENT_ROLLUP did NOT enqueue"

grep -q "TriggerReceiver: Enqueued DistanceDaily (once-DistanceDaily)" "$LOGT" \
  && pass "Trigger ACTION_RUN_DISTANCE_DAILY enqueued worker" \
  || fail "Trigger ACTION_RUN_DISTANCE_DAILY did NOT enqueue"

# ---- Summary exit code ----
if toybox grep -q "❌" "$OUT"; then exit 1; else exit 0; fi
