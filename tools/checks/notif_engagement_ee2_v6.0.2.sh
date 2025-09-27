#!/bin/sh
set -eu

PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/notification_engagement/ee2.2.txt"
NAME="EngagementRollup"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "EE-2 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG"  >/dev/null 2>&1 || { echo "EE-2 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

adb logcat -c >/dev/null 2>&1 || true
sleep 1

MAN_REC="$(adb shell dumpsys package "$PKG" 2>/dev/null \
  | grep -iE 'BOOT_COMPLETED|PACKAGE_REPLACED' -n || true)"

JS="$(adb shell dumpsys jobscheduler 2>/dev/null \
  | awk -v p="$PKG" -v n="$NAME" 'BEGIN{IGNORECASE=1}
     /JOB #|u[0-9]+a[0-9]+/{blk=""}
     {blk=blk $0 "\n"}
     /JobInfo/{ if(blk ~ p && blk ~ n) print blk > "/dev/stderr" }
   ' 2>&1 || true)"

WM="$(adb shell dumpsys activity service WorkManager 2>/dev/null \
  | grep -i "$PKG" -A20 | grep -iE "$NAME|Engagement|Notif" -m1 || true)"

sleep 1
LOG="$(adb logcat -d 2>/dev/null \
  | grep -iE 'Boot|Package|Receiver|WorkManager|Enqueue|Engagement' || true)"

{
  echo "=== MANIFEST RECEIVERS (BOOT/REPLACED) ==="
  [ -n "$MAN_REC" ] && echo "$MAN_REC" || echo "[none]"
  echo
  echo "=== JOBSCHEDULER MATCH ==="
  [ -n "$JS" ] && echo "$JS" || echo "[none]"
  echo
  echo "=== WORKMANAGER MATCH ==="
  [ -n "$WM" ] && echo "$WM" || echo "[none]"
  echo
  echo "=== LOGCAT (boot/update/enqueue) ==="
  [ -n "$LOG" ] && echo "$LOG" || echo "[none]"
} | tee "$OUT" >/dev/null

if [ -n "$JS" ] || [ -n "$WM" ]; then
  echo "EE-2 RESULT=PASS" | tee -a "$OUT"
  exit 0
fi

echo "EE-2 RESULT=FAIL" | tee -a "$OUT"
exit 1
