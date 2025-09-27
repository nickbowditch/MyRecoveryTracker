#!/bin/sh
set -eu

APP="com.nick.myrecoverytracker"
RCV="$APP/.TriggerReceiver"
CSV="files/daily_lnslu.csv"
OUT="evidence/v6.0/lnsu/gv7.2.txt"

mkdir -p "$(dirname "$OUT")"

adb logcat -c >/dev/null 2>&1 || true
for ACT in "$APP.ACTION_RUN_LNSU_ROLLUP" "$APP.ACTION_RUN_ROLLUP_LNSU"; do
adb shell cmd activity broadcast -n "$RCV" -a "$ACT" --receiver-foreground --user 0 >/dev/null 2>&1 || true
done
sleep 2

LOG="$(adb logcat -d | grep -i -E 'TriggerReceiver|Lnsu|Late.?Night|Screen.Usage' || true)"
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

if printf '%s\n' "$TAIL" | grep -Eq '^[0-9]{4}-[0-9]{2}-[0-9]{2},-?[0-9]+(#[^,])?$|^\[MISSING:'; then
if printf '%s\n' "$TAIL" | grep -q '^\[MISSING:'; then
echo "GV7 RESULT=FAIL (missing CSV)" | tee -a "$OUT"
exit 1
fi
echo "GV7 RESULT=PASS" | tee -a "$OUT"
exit 0
else
echo "GV7 RESULT=FAIL (bad or missing CSV rows)" | tee -a "$OUT"
exit 1
fi
