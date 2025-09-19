#!/bin/bash
PKG="${PKG:-com.nick.myrecoverytracker}"
S=0

adb get-state >/dev/null 2>&1 || { echo "EE-3 RESULT: FAIL (no device)"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "EE-3 RESULT: FAIL (app not installed)"; exit 3; }

adb logcat -c
adb shell cmd deviceidle force-idle >/dev/null 2>&1 || true
sleep 5

SJ="androidx.work.impl.background.systemjob.SystemJobService"
RUNNING="$(adb shell dumpsys jobscheduler 2>/dev/null | tr -d $'\r' | awk -v p="$PKG/$SJ" '
  $0 ~ p {inblk=1}
  inblk && /Running for:/ {c++}
  inblk && NF==0 {inblk=0}
  END{print c+0}')"

FG="$(adb shell dumpsys activity services 2>/dev/null | tr -d $'\r' | awk -v p="$PKG/.ForegroundUnlockService" '
  $0 ~ p {inblk=1}
  inblk && /isForeground=true/ {print "yes"; exit}
  inblk && NF==0 {inblk=0}
  END{if(NR==0) print "no"}')"

LOGPATTERN='(WorkManager|WM-|SystemJobService|Reschedule|Unlock(Rollup|Validation)Worker)'
LOGHIT="$(adb shell logcat -d -v brief 2>/dev/null | egrep -i "$LOGPATTERN" | tail -n 100 | wc -l | tr -d ' ')"

echo "EE-3 JOBS_RUNNING=$RUNNING FG_UNLOCK=$FG LOG_HITS=$LOGHIT"

if [ "$RUNNING" -gt 0 ] || [ "$FG" = "yes" ] || [ "$LOGHIT" -gt 0 ]; then
  echo "EE-3 RESULT: PASS"
  exit 0
else
  echo "EE-3 RESULT: FAIL"
  echo "--- jobs ---"
  adb shell dumpsys jobscheduler 2>/dev/null | tr -d $'\r' | sed -n "/$PKG\/$SJ/,+20p"
  echo "--- services ---"
  adb shell dumpsys activity services 2>/dev/null | tr -d $'\r' | sed -n "/$PKG\/.ForegroundUnlockService/,+20p"
  echo "--- recent logs ---"
  adb shell logcat -d -v brief 2>/dev/null | egrep -i "$LOGPATTERN" | tail -n 100
  exit 1
fi
