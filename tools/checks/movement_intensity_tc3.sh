#!/bin/bash
set -euo pipefail
PKG="com.nick.myrecoverytracker"
CSV="files/daily_movement_intensity.csv"
OUT="evidence/v6.0/movement_intensity/tc3.txt"

mkdir -p "$(dirname "$OUT")"
exec > >(tee "$OUT") 2>&1
fail(){ echo "TC3 RESULT=FAIL ($1)"; exit 1; }

adb get-state >/dev/null 2>&1 || { echo "TC3 RESULT=FAIL (no device)"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "TC3 RESULT=FAIL (app not installed)"; exit 3; }

HDR="$(adb exec-out run-as "$PKG" sed -n '1p' "$CSV" 2>/dev/null | tr -d '\r' || true)"
[ "$HDR" = "date,intensity" ] || fail "daily CSV header mismatch or missing"

TODAY="$(adb shell 'toybox date +%F 2>/dev/null || date +%F' | tr -d $'\r')"
[ -n "$TODAY" ] || fail "date read error"

TMP="$(mktemp)"
adb exec-out run-as "$PKG" cat "$CSV" 2>/dev/null | tr -d '\r' > "$TMP" || true
[ -s "$TMP" ] || fail "CSV missing or empty"

OFF_COUNT="$(awk -F, -v tdy="$TODAY" '
  NR==1{next}
  $1 ~ /^[0-9]{4}-[0-9]{2}-[0-9]{2}$/ && $1>tdy {c++}
  END{print c+0}
' "$TMP")"

echo "today=$TODAY"
echo "--- DEBUG: CSV head ---"
head -n 10 "$TMP"
echo "--- DEBUG: future-dated rows (count=$OFF_COUNT) ---"
awk -F, -v tdy="$TODAY" 'NR==1{next} $1 ~ /^[0-9]{4}-[0-9]{2}-[0-9]{2}$/ && $1>tdy' "$TMP" | head -n 20

rm -f "$TMP"

if [ "$OFF_COUNT" -gt 0 ]; then
  fail "found $OFF_COUNT future-dated rows"
fi

echo "TC3 RESULT=PASS"
exit 0
