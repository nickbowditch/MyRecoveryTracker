#!/bin/bash
PKG="${PKG:-com.nick.myrecoverytracker}"
MODE="${1:-check}"
S=0

case "$MODE" in
  prep)
    adb get-state >/dev/null 2>&1 || { echo "EE-2 RESULT: FAIL (no device/emulator)"; exit 2; }
    adb logcat -c
    echo "EE-2 PREP: READY"
    exit 0
    ;;
  check)
    adb wait-for-device >/dev/null 2>&1 || { echo "EE-2 RESULT: FAIL (no device/emulator)"; exit 2; }
    adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "EE-2 RESULT: FAIL (app not installed)"; exit 3; }

    adb shell monkey -p "$PKG" -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1 || true

    SJ="androidx.work.impl.background.systemjob.SystemJobService"
    JOBS=0
    for i in {1..6}; do
      sleep 5
      JOBS="$(adb shell dumpsys jobscheduler 2>/dev/null | tr -d $'\r' | grep -F "$PKG/$SJ" | wc -l | tr -d ' ')"
      [ "$JOBS" -gt 0 ] && break
    done

    LOGPATTERN='(WorkManager|WM-|Reschedule|RescheduleReceiver|ForceStopRunnable|BOOT_COMPLETED|PACKAGE_REPLACED|enqueue|Unlock(Rollup|Validation)Worker)'
    LOG="$(adb shell logcat -d -v brief 2>/dev/null | egrep -i "$LOGPATTERN" | tail -n 200)"

    if [ "$JOBS" -gt 0 ] && [ -n "$LOG" ]; then
      echo "EE-2 PASS: jobs present after boot/update"
      echo "EE-2 RESULT: PASS"
      exit 0
    else
      echo "EE-2 FAIL: no evidence after boot/update"
      echo "--- jobs ---"
      adb shell dumpsys jobscheduler 2>/dev/null | tr -d $'\r' | grep -nF "$PKG/$SJ" || true
      echo "--- log ---"
      echo "$LOG"
      echo "EE-2 RESULT: FAIL"
      exit 1
    fi
    ;;
  *)
    echo "EE-2 RESULT: FAIL (bad mode)"; exit 4
    ;;
esac
