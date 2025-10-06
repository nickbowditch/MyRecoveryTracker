#!/bin/bash
set -euo pipefail
PKG="com.nick.myrecoverytracker"
RCV="$PKG/.TriggerReceiver"
CSV="files/daily_movement_intensity.csv"
OUT="evidence/v6.0/movement_intensity/ee3.txt"

mkdir -p "$(dirname "$OUT")"
exec > >(tee "$OUT") 2>&1

fail(){ echo "EE3 RESULT=FAIL ($1)"; exit 1; }

adb get-state >/dev/null 2>&1 || { echo "EE3 RESULT=FAIL (no device)"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "EE3 RESULT=FAIL (app not installed)"; exit 3; }

TODAY="$(adb shell 'toybox date +%F 2>/dev/null || date +%F' | tr -d $'\r')"
[ -n "$TODAY" ] || fail "date read error"

adb logcat -c >/dev/null 2>&1 || true
adb exec-out run-as "$PKG" rm -f "$CSV" >/dev/null 2>&1 || true

adb shell am broadcast -n "$RCV" -a "$PKG.ACTION_RUN_MOVEMENT_INTENSITY" >/dev/null 2>&1 || true

adb shell dumpsys jobscheduler \
| sed -n 's/.*#u[0-9a-zA-Z]\+\/\([0-9]\+\).*/\1/p' \
| while read -r JID; do
    adb shell cmd jobscheduler run -f "$PKG" "$JID" >/dev/null 2>&1 || true
  done

deadline=$((SECONDS+20))
FOUND=""
while (( SECONDS < deadline )); do
  HDR="$(adb exec-out run-as "$PKG" sh -c 'sed -n "1p" "'"$CSV"'" 2>/dev/null | tr -d "\r"' || true)"
  if [ "$HDR" = "date,intensity" ]; then
    ROW="$(adb exec-out run-as "$PKG" sh -c 'grep -E "^'"$TODAY"'," "'"$CSV"'" 2>/dev/null | head -n1 | tr -d "\r"' || true)"
    [ -n "$ROW" ] && FOUND="$ROW" && break
  fi
  sleep 1
done

echo "csv_path=$CSV"
echo "header=${HDR:-<none>}"
echo "today=$TODAY"
echo "row_today=${FOUND:-<none>}"
adb exec-out run-as "$PKG" sh -c 'head -n 10 "'"$CSV"'" 2>/dev/null | tr -d "\r"' || true

[ -n "$FOUND" ] || fail "no row for today"
echo "EE3 RESULT=PASS"
exit 0
