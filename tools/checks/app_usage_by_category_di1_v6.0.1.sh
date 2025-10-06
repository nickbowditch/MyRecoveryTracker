#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
CSV="files/app_category_daily.csv"
LOCK="app/locks/app_category_daily.header"
OUT="evidence/v6.0/app_usage_by_category/di1.1.txt"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "DI-1 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "DI-1 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

EXP="$(tr -d '\r' < "$LOCK" 2>/dev/null || true)"
[ -n "$EXP" ] || { echo "DI-1 RESULT=FAIL (missing lock)" | tee "$OUT"; exit 4; }

HDR="$(adb exec-out run-as "$PKG" head -n1 "$CSV" 2>/dev/null | tr -d '\r' || true)"
[ -n "$HDR" ] || { echo "DI-1 RESULT=FAIL (missing csv)" | tee "$OUT"; exit 5; }

[ "$HDR" = "$EXP" ] || { echo "DI-1 RESULT=FAIL (header drift)" | tee "$OUT"; exit 6; }
echo "DI-1 RESULT=PASS" | tee "$OUT"
exit 0
