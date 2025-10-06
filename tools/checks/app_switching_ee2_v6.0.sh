#!/bin/sh
set -eu

PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/app_switching/ee2.txt"
RCV="$PKG/.TriggerReceiver"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "EE-2 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG"  >/dev/null 2>&1 || { echo "EE-2 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

ACT1="$(grep -R --include='*.kt' --include='*.java' -nE '"com\.nick\.myrecoverytracker\.ACTION_[A-Z0-9_]*SWITCH[A-Z0-9_]*"' app/src/main/java 2>/dev/null | sed -E 's/.*"([^"]+)".*/\1/' | head -n1 || true)"

adb logcat -c >/dev/null 2>&1 || true
[ -n "$ACT1" ] && adb shell cmd activity broadcast -n "$RCV" -a "$ACT1" --receiver-foreground --user 0 >/dev/null 2>&1 || true
sleep 2

JS="$(adb shell dumpsys jobscheduler 2>/dev/null \
| awk -v p="$PKG" 'BEGIN{IGNORECASE=1; blk=""}
^JOB #[0-9]+/{blk=$0 ORS; next}
{blk=blk $0 ORS}
/(READY|WAITING|RUNNABLE|RTC|SCHEDULED)/{
  if (blk ~ p && blk ~ /(App.*Switch|Switch.*Daily|AppSwitch|Switching)/) {print blk; exit}
}' || true)"

WM="$(adb shell dumpsys activity service WorkManager 2>/dev/null \
| awk -v p="$PKG" 'BEGIN{IGNORECASE=1}
($0 ~ p) && ($0 ~ /(App.*Switch|Switch.*Daily|AppSwitch|Switching)/){print; exit}' || true)"

LOG="$(adb logcat -d 2>/dev/null \
| grep -iE 'TriggerReceiver|WorkManager|enqueue|(App.*Switch|Switch.*Daily|AppSwitch|Switching)' || true)"

{
  echo "ACTION=${ACT1:-[none found]}"
  echo
  echo "=== JOBSCHEDULER ==="
  [ -n "$JS" ] && echo "$JS" || echo "[none]"
  echo
  echo "=== WORKMANAGER ==="
  [ -n "$WM" ] && echo "$WM" || echo "[none]"
  echo
  echo "=== LOGCAT ==="
  [ -n "$LOG" ] && echo "$LOG" || echo "[none]"
} | tee "$OUT" >/dev/null

if [ -n "$JS" ] || [ -n "$WM" ] || printf '%s' "$LOG" | grep -qiE '(App.*Switch|Switch.*Daily|AppSwitch|Switching).(enqueue|worker|rollup|start|run|success|succeed|completed|finish)'; then
  echo "EE-2 RESULT=PASS" | tee -a "$OUT"; exit 0
else
  echo "EE-2 RESULT=FAIL" | tee -a "$OUT"; exit 1
fi
