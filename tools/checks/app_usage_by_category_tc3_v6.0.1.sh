#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
CSV="files/app_category_daily.csv"
OUT="evidence/v6.0/app_usage_by_category/tc3.1.txt"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "TC-3 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG"  >/dev/null 2>&1 || { echo "TC-3 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

HDR="$(adb exec-out run-as "$PKG" head -n1 "$CSV" 2>/dev/null | tr -d '\r' || true)"
[ -n "$HDR" ] || { echo "TC-3 RESULT=FAIL (missing csv)" | tee "$OUT"; exit 4; }
[ "$HDR" = "date,category,minutes" ] || { echo "TC-3 RESULT=FAIL (bad header)" | tee "$OUT"; exit 5; }

TODAY="$(adb shell date +%F 2>/dev/null | tr -d '\r')"
BAD="$(adb exec-out run-as "$PKG" awk -F, -v t="$TODAY" 'NR>1 && $1>t{print}' "$CSV" 2>/dev/null | tr -d '\r' || true)"

if [ -z "$BAD" ]; then
  echo "TC-3 RESULT=PASS" | tee "$OUT"
  exit 0
else
  echo "TC-3 RESULT=FAIL" | tee "$OUT"
  printf '%s\n' "$BAD" >> "$OUT"
  exit 1
fi
