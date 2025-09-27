#!/bin/sh
set -eu

APP="com.nick.myrecoverytracker"
RCV="$APP/.TriggerReceiver"
ACT="$APP.ACTION_RUN_UNLOCK_ROLLUP"
CSV="files/daily_unlocks.csv"
OUT="evidence/v6.0/unlocks/gv7.1.txt"

mkdir -p "$(dirname "$OUT")"

adb logcat -c
adb shell cmd activity broadcast -n "$RCV" -a "$ACT" --receiver-foreground --user 0 >/dev/null 2>&1 || true

{
  echo "=== LOG ==="
  adb logcat -d | grep -i -E 'TriggerReceiver|UnlockRollup' || true
  echo
  echo "=== TAIL: $CSV ==="
  adb exec-out run-as "$APP" sh -c '
    f="'"$CSV"'"
    if [ -f "$f" ]; then
      tail -n 5 "$f" | tr -d "\r"
    else
      echo "[MISSING: '"$CSV"']"
    fi
  '
} | tee "$OUT"
