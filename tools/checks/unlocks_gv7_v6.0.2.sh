#!/bin/sh
set -eu

APP="com.nick.myrecoverytracker"
RCV="$APP/.TriggerReceiver"
ACT="$APP.ACTION_RUN_UNLOCK_ROLLUP"
CSV="files/daily_unlocks.csv"
OUT="evidence/v6.0/unlocks/gv7.2.txt"

mkdir -p "$(dirname "$OUT")"

adb logcat -c
adb shell cmd activity broadcast -n "$RCV" -a "$ACT" --receiver-foreground --user 0 >/dev/null 2>&1 || true
sleep 2

LOG="$(adb logcat -d | grep -i -E 'TriggerReceiver|UnlockRollup' || true)"
TAIL="$(adb exec-out run-as "$APP" sh -c '
f="'"$CSV"'"
if [ -f "$f" ]; then
tail -n 5 "$f" | tr -d "\r"
else
echo "[MISSING: '"$CSV"']"
fi
')"

{
echo "=== LOG ==="
echo "$LOG"
echo
echo "=== TAIL: $CSV ==="
echo "$TAIL"
} | tee "$OUT"

if [ -z "$LOG" ]; then
echo "GV7 RESULT=FAIL (no log)" | tee -a "$OUT"
exit 1
fi

if printf '%s\n' "$TAIL" | grep -q '^[0-9]\{4\}-[0-9]\{2\}-[0-9]\{2\},v6\.0,[0-9]\+'; then
echo "GV7 RESULT=PASS" | tee -a "$OUT"
exit 0
else
echo "GV7 RESULT=FAIL (bad or missing CSV rows)" | tee -a "$OUT"
exit 1
fi
