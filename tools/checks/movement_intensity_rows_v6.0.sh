#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
CSV="files/daily_movement_intensity.csv"
OUT="evidence/v6.0/movement/movement_intensity_rows.txt"
mkdir -p "$(dirname "$OUT")"
exec >"$OUT" 2>&1
adb get-state >/dev/null 2>&1 || { echo "RESULT=FAIL (no device)"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "RESULT=FAIL (app not installed)"; exit 3; }
TODAY="$(adb shell 'toybox date +%F 2>/dev/null || date +%F' | tr -d '\r')"
if ! adb exec-out run-as "$PKG" sh -c "test -f $CSV" >/dev/null 2>&1; then
  echo "RESULT=FAIL (no CSV found)"
  exit 1
fi
HEADER="$(adb exec-out run-as "$PKG" head -n 1 "$CSV" | tr -d '\r')"
ROWS_TODAY="$(adb exec-out run-as "$PKG" awk -F',' -v d="$TODAY" 'NR>1 && $1==d {c++} END{print c+0}' "$CSV" | tr -d '\r')"
echo "HEADER: $HEADER"
echo "TODAY: $TODAY"
echo "ROWS_TODAY: $ROWS_TODAY"
adb exec-out run-as "$PKG" tail -n 5 "$CSV" | tr -d '\r' | sed 's/^/  /'
if [ "$ROWS_TODAY" -gt 0 ]; then
  echo "RESULT=PASS"
  exit 0
else
  echo "RESULT=FAIL"
  exit 1
fi
