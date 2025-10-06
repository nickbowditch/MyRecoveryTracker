#!/bin/sh
set -eu

PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/app_usage_by_category/ee2.txt"
RCV="$PKG/.TriggerReceiver"
ACT1="$PKG.ACTION_RUN_APP_CATEGORY_DAILY"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "EE-2 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG"  >/dev/null 2>&1 || { echo "EE-2 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

adb logcat -c >/dev/null 2>&1 || true
adb shell cmd activity broadcast -n "$RCV" -a "$ACT1" --receiver-foreground --user 0 >/dev/null 2>&1 || true
sleep 2

JS="$(adb shell dumpsys jobscheduler 2>/dev/null \
| awk -v p="$PKG" 'BEGIN{IGNORECASE=1; blk=""}
/^JOB #/{blk=$0 ORS; next}
{blk=blk $0 ORS}
/(READY|WAITING|RUNNABLE|RTC)/{
 if (blk ~ p && blk ~ /(App.*Category|Category.*Daily|AppUsage.*Category)/) {print blk; exit}
}' || true)"

WM="$(adb shell dumpsys activity service WorkManager 2>/dev/null \
| awk -v p="$PKG" 'BEGIN{IGNORECASE=1}
($0 ~ p) && ($0 ~ /(App.*Category|Category.*Daily|AppUsage.*Category)/){print; exit}' || true)"

LOG="$(adb logcat -d 2>/dev/null \
| grep -iE 'TriggerReceiver|WorkManager|enqueue|(App.*Category|Category.*Daily|AppUsage.*Category)' || true)"

{
echo "=== JOBSCHEDULER ==="
[ -n "$JS" ] && echo "$JS" || echo "[none]"
echo
echo "=== WORKMANAGER ==="
[ -n "$WM" ] && echo "$WM" || echo "[none]"
echo
echo "=== LOGCAT ==="
[ -n "$LOG" ] && echo "$LOG" || echo "[none]"
} | tee "$OUT" >/dev/null

if [ -n "$JS" ] || [ -n "$WM" ] || printf '%s' "$LOG" | grep -qiE '(App.*Category|Category.*Daily|AppUsage.*Category).*(enqueue|worker|rollup|start|run)'; then
  echo "EE-2 RESULT=PASS" | tee -a "$OUT"; exit 0
else
  echo "EE-2 RESULT=FAIL" | tee -a "$OUT"; exit 1
fi
