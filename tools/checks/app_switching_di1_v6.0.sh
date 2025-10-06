#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
CSV="files/daily_app_switching.csv"
LOCK="app/locks/daily_app_switching.header"
OUT="evidence/v6.0/app_switching/di1.txt"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "DI-1 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "DI-1 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

EXP="$(tr -d '\r' < "$LOCK" 2>/dev/null || true)"
[ -n "$EXP" ] || {
  {
    echo "DI-1 RESULT=FAIL (missing lock)"
    echo "--- DEBUG ---"
    echo "LOCK_PATH=$LOCK"
  } | tee "$OUT"
  exit 4
}

HDR="$(adb exec-out run-as "$PKG" head -n1 "$CSV" 2>/dev/null | tr -d '\r' || true)"
[ -n "$HDR" ] || {
  {
    echo "DI-1 RESULT=FAIL (missing csv)"
    echo "--- DEBUG ---"
    echo "CSV_PATH=$CSV"
    echo "APP_FILES_LS:"
    adb exec-out run-as "$PKG" ls -l files 2>/dev/null || echo "[cannot list files/]"
  } | tee "$OUT"
  exit 5
}

if [ "$HDR" = "$EXP" ]; then
  echo "DI-1 RESULT=PASS" | tee "$OUT"
  exit 0
else
  {
    echo "DI-1 RESULT=FAIL (header drift)"
    echo "--- DEBUG ---"
    echo "EXPECTED: $EXP"
    echo "ACTUAL:   $HDR"
  } | tee "$OUT"
  exit 6
fi
