#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
CSV="files/daily_movement_intensity.csv"
OUT="evidence/v6.0/movement_intensity/at1.txt"
mkdir -p "$(dirname "$OUT")"
exec > >(tee "$OUT") 2>&1
fail(){ echo "AT-1 RESULT=FAIL ($1)"; exit 1; }

adb get-state >/dev/null 2>&1 || fail "no device"
adb shell pm path "$PKG" >/dev/null 2>&1 || fail "app not installed"

HDR="$(adb exec-out run-as "$PKG" sed -n '1p' "$CSV" 2>/dev/null | tr -d '\r' || true)"
[ "$HDR" = "date,intensity" ] || fail "header mismatch"

TODAY="$(adb shell 'toybox date +%F 2>/dev/null || date +%F' | tr -d '\r' || true)"
[ -n "$TODAY" ] || fail "no date"

PRE="$(adb exec-out run-as "$PKG" awk -F, -v d="$TODAY" 'NR>1&&$1==d{print;exit}' "$CSV" 2>/dev/null | tr -d '\r' || true)"
echo "pre_row=${PRE:-<none>}"

adb shell cmd activity broadcast -n "$PKG/.TriggerReceiver" -a "$PKG.ACTION_RUN_MOVEMENT_INTENSITY" >/dev/null 2>&1 || true

sleep 2

POST="$(adb exec-out run-as "$PKG" awk -F, -v d="$TODAY" 'NR>1&&$1==d{print;exit}' "$CSV" 2>/dev/null | tr -d '\r' || true)"
echo "post_row=${POST:-<none>}"

[ -n "$POST" ] || fail "no row for today"
[ "$PRE" != "$POST" ] || echo "row unchanged (still valid)"

echo "--- CSV tail ---"
adb exec-out run-as "$PKG" tail -n 5 "$CSV" 2>/dev/null | tr -d '\r' || true

CNT="$(printf '%s\n' "$POST" | awk -F, '{print $2}')"
printf '%s\n' "$CNT" | grep -Eq '^[0-9]+$' || fail "intensity not integer"
awk -v c="$CNT" 'BEGIN{exit !(c>=0 && c<=10000)}' || fail "intensity out of bounds"

echo "AT-1 RESULT=PASS"
exit 0
