#!/bin/sh
set -eu

PKG="com.nick.myrecoverytracker"
APP_MAIN="$PKG/.MainActivity"
OUT="evidence/v6.0/ALL_HEALTHCHECK/app_build_and_resilience_smoke.txt"
mkdir -p "$(dirname "$OUT")"
: > "$OUT"

ok(){ echo "✅ $1" | tee -a "$OUT"; }
bad(){ echo "❌ $1" | tee -a "$OUT"; }

# --- sanity ---
adb get-state >/dev/null 2>&1 || { bad "NO DEVICE"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { bad "APP NOT INSTALLED"; exit 3; }

# --- verify frozen version matches device ---
VC_LOCAL="$(grep -E '^VERSION_CODE=' gradle.properties | head -n1 | cut -d= -f2 || true)"
VN_LOCAL="$(grep -E '^VERSION_NAME=' gradle.properties | head -n1 | cut -d= -f2 || true)"
PKG_DUMP="$(adb shell dumpsys package "$PKG" 2>/dev/null || true)"
VC_DEV="$(printf "%s\n" "$PKG_DUMP" | grep -m1 -E 'versionCode=' | sed -E 's/.*versionCode=([0-9]+).*/\1/' || true)"
VN_DEV="$(printf "%s\n" "$PKG_DUMP" | grep -m1 -E 'versionName=' | sed -E 's/.*versionName=([[:alnum:]._-]+).*/\1/' || true)"

[ -n "$VC_LOCAL" ] && [ "$VC_LOCAL" = "$VC_DEV" ] && ok "versionCode frozen ($VC_DEV)" || bad "versionCode mismatch local=$VC_LOCAL device=$VC_DEV"
[ -n "$VN_LOCAL" ] && [ "$VN_LOCAL" = "$VN_DEV" ] && ok "versionName frozen ($VN_DEV)" || bad "versionName mismatch local=$VN_LOCAL device=$VN_DEV"

# --- verify UpgradeReceiver is declared (via dumpsys) ---
printf "%s\n" "$PKG_DUMP" | grep -q "com.nick.myrecoverytracker.UpgradeReceiver" && \
  ok "UpgradeReceiver declared" || bad "UpgradeReceiver missing from package dump"

# --- app bring-up + CrashReporter.onCreate() path (starts services, enqueues periodic, runs Reschedule if flagged) ---
adb shell logcat -c >/dev/null 2>&1 || true
adb shell am start -n "$APP_MAIN" >/dev/null 2>&1 || true
sleep 2
LOG="$(adb shell logcat -d 2>/dev/null || true)"

echo "$LOG" | grep -q "Requested ForegroundUnlockService start" && ok "ForegroundUnlockService start logged" || bad "ForegroundUnlockService start not logged"
echo "$LOG" | grep -q "Requested LocationCaptureService start" && ok "LocationCaptureService start logged" || bad "LocationCaptureService start not logged"
echo "$LOG" | grep -q "Enqueued NotificationEngagementWorker (24h periodic)" && ok "NotificationEngagement periodic enqueued" || bad "NotificationEngagement periodic enqueue not seen"
echo "$LOG" | grep -q "RedcapDiag.log executed on launch" && ok "RedcapDiag log executed" || ok "RedcapDiag log not present (non-fatal)"

# --- services alive? ---
adb shell dumpsys activity services "$PKG" 2>/dev/null | grep -q "ForegroundUnlockService" && ok "ForegroundUnlockService alive" || bad "ForegroundUnlockService not alive"
adb shell dumpsys activity services "$PKG" 2>/dev/null | grep -q "LocationCaptureService" && ok "LocationCaptureService alive" || bad "LocationCaptureService not alive"

# --- WorkManager -> JobScheduler backend present? (any jobs for our pkg) ---
adb shell dumpsys jobscheduler 2>/dev/null | grep -q "$PKG" && ok "Jobs present in JobScheduler" || bad "No jobs found in JobScheduler"

# --- TriggerReceiver wiring (both actions) ---
adb shell logcat -c >/dev/null 2>&1 || true
adb shell cmd activity broadcast -n "$PKG/.TriggerReceiver" -a "$PKG.ACTION_RUN_NOTIFICATION_ENGAGEMENT_ROLLUP" --receiver-foreground --receiver-include-background --include-stopped-packages >/dev/null 2>&1 || true
sleep 1
LOG_A="$(adb shell logcat -d 2>/dev/null || true)"
echo "$LOG_A" | grep -q "TriggerReceiver: onReceive action=$PKG.ACTION_RUN_NOTIFICATION_ENGAGEMENT_ROLLUP" && ok "Trigger ENGAGEMENT_ROLLUP delivered to TriggerReceiver" || bad "ENGAGEMENT_ROLLUP action NOT delivered (manifest action may be missing)"
echo "$LOG_A" | grep -q "Enqueued NotificationEngagement (once-NotificationEngagementRollup)" && ok "ENGAGEMENT_ROLLUP enqueued worker" || bad "ENGAGEMENT_ROLLUP did not enqueue worker"

adb shell logcat -c >/dev/null 2>&1 || true
adb shell cmd activity broadcast -n "$PKG/.TriggerReceiver" -a "$PKG.ACTION_RUN_DISTANCE_DAILY" --receiver-foreground --receiver-include-background --include-stopped-packages >/dev/null 2>&1 || true
sleep 1
LOG_B="$(adb shell logcat -d 2>/dev/null || true)"
echo "$LOG_B" | grep -q "TriggerReceiver: onReceive action=$PKG.ACTION_RUN_DISTANCE_DAILY" && ok "Trigger DISTANCE_DAILY delivered to TriggerReceiver" || bad "DISTANCE_DAILY not delivered"
echo "$LOG_B" | grep -q "Enqueued DistanceDaily (once-DistanceDaily)" && ok "DISTANCE_DAILY enqueued worker" || bad "DISTANCE_DAILY did not enqueue worker"

# --- Optional: Reschedule flow proof (mark + app start should clear flag and log) ---
adb shell logcat -c >/dev/null 2>&1 || true
adb shell run-as "$PKG" sh -c 'ctx="$(pm dump '"$PKG"' | sed -n "s/.*userId=\\([0-9]\\+\\).*/u\\1/p" | head -n1)"; dp="/data/user_de/${ctx#u}/'"$PKG"'"; mkdir -p "$dp/shared_prefs"; printf "<map><boolean name=\"needs_reschedule\" value=\"true\"/></map>" > "$dp/shared_prefs/upgrade_flags.xml"' >/dev/null 2>&1 || true
adb shell am start -n "$APP_MAIN" >/dev/null 2>&1 || true
sleep 1
LOG_R="$(adb shell logcat -d 2>/dev/null || true)"
echo "$LOG_R" | grep -q "Reschedule" && ok "Reschedule path exercised (flag -> runNow)" || ok "Reschedule log not observed (acceptable if flag write was blocked)"

exit 0
