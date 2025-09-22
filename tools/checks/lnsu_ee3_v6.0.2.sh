#!/bin/sh
PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/lnsu/ee3.2.txt"
CSV="files/daily_lnsu.csv"
LOCK="app/locks/daily_lnsu.header"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "EE-3 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "EE-3 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

EXP="$(tr -d '\r' < "$LOCK" 2>/dev/null)"
[ -n "$EXP" ] || { echo "EE-3 RESULT=FAIL (missing lock)" | tee "$OUT"; exit 4; }

HDR="$(adb exec-out run-as "$PKG" head -n1 "$CSV" 2>/dev/null | tr -d '\r')"
[ "$HDR" = "$EXP" ] || { echo "EE-3 RESULT=FAIL (bad or missing header)" | tee "$OUT"; exit 5; }

echo "EE-3 RESULT=PASS" | tee "$OUT"
exit 0
