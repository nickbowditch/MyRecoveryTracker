#!/bin/bash
set -euo pipefail
PKG="com.nick.myrecoverytracker"
RCV="$PKG/.TriggerReceiver"
ACT="$PKG.ACTION_RUN_MOVEMENT_INTENSITY"
CSV="files/daily_movement_intensity.csv"
OUT="evidence/v6.0/movement_intensity/gv7.txt"

mkdir -p "$(dirname "$OUT")"
exec > >(tee "$OUT") 2>&1
fail(){ echo "GV7 RESULT=FAIL ($1)"; exit 1; }

echo "started_at=$(date -u +'%Y-%m-%dT%H:%M:%SZ')"

adb get-state >/dev/null 2>&1 || fail "no device"
adb shell pm path "$PKG" >/dev/null 2>&1 || fail "app not installed"

TODAY="$(adb shell 'toybox date +%F 2>/dev/null || date +%F' | tr -d $'\r')"
[ -n "$TODAY" ] || fail "date read error"
echo "target_day=$TODAY"

HDR="$(adb exec-out run-as "$PKG" sed -n '1p' "$CSV" 2>/dev/null | tr -d '\r' || true)"
[ -n "$HDR" ] || echo "note: CSV not present yet; will attempt to create via manual trigger"
[ -n "$HDR" ] && echo "pre_header=$HDR"

adb shell cmd activity broadcast -n "$RCV" -a "$ACT" --receiver-foreground --user 0 >/dev/null 2>&1 || true

adb shell dumpsys jobscheduler \
| sed -n 's/.*#u[0-9a-zA-Z]\+\/\([0-9]\+\).*/\1/p' \
| while read -r JID; do
    adb shell cmd jobscheduler run -f "$PKG" "$JID" >/dev/null 2>&1 || true
  done

deadline=$((SECONDS+20))
FOUND=""
while (( SECONDS < deadline )); do
  CUR_HDR="$(adb exec-out run-as "$PKG" sed -n '1p' "$CSV" 2>/dev/null | tr -d '\r' || true)"
  if [ "$CUR_HDR" = "date,intensity" ]; then
    ROW="$(adb exec-out run-as "$PKG" awk -F, -v d="$TODAY" 'NR>1 && $1==d{print; exit}' "$CSV" 2>/dev/null | tr -d '\r' || true)"
    if [ -n "$ROW" ]; then FOUND="$ROW"; break; fi
  fi
  sleep 1
done

echo "csv_header=${CUR_HDR:-<none>}"
echo "row_today=${FOUND:-<none>}"

echo "--- DEBUG: CSV head/tail ---"
adb exec-out run-as "$PKG" sh -c 'head -n 10 "'"$CSV"'" 2>/dev/null | tr -d "\r"' || true
adb exec-out run-as "$PKG" sh -c 'tail -n 10 "'"$CSV"'" 2>/dev/null | tr -d "\r"' || true

[ "$CUR_HDR" = "date,intensity" ] || fail "header mismatch or missing"
[ -n "$FOUND" ] || fail "no row for target day after manual trigger"

VAL_OK="$(printf '%s\n' "$FOUND" | awk -F, 'NF==2 && $2 ~ /^[0-9]+$/ && $2+0>=0 {print "ok"}')"
[ "$VAL_OK" = "ok" ] || fail "row format invalid (expected date,intensity with non-negative integer)"

echo "GV7 RESULT=PASS"
exit 0
