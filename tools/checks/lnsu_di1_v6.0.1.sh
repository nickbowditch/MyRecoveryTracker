#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/lnsu/di1.1.txt"
CSV="files/daily_lnsu.csv"
LOCK="app/locks/daily_lnsu.header"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "DI-1 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "DI-1 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

EXP="$(tr -d '\r' < "$LOCK" 2>/dev/null || true)"
[ -n "$EXP" ] || { echo "DI-1 RESULT=FAIL (missing lock header)" | tee "$OUT"; exit 4; }

HDR="$(adb exec-out run-as "$PKG" head -n1 "$CSV" 2>/dev/null | tr -d '\r' || true)"
[ -n "$HDR" ] || { echo "DI-1 RESULT=FAIL (missing csv)" | tee "$OUT"; exit 5; }

if [ "$HDR" = "$EXP" ]; then
echo "DI-1 RESULT=PASS" | tee "$OUT"
exit 0
fi

{
echo "DI-1 RESULT=FAIL (drift)"
echo "LOCK=$EXP"
echo "HEAD=$HDR"
} | tee "$OUT"
exit 1
