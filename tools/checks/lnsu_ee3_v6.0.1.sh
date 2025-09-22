#!/bin/sh
PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/lnsu/ee3.1.txt"
CSV="files/daily_lnsu.csv"
EXP="date,feature_schema_version,minutes_22_02"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "EE-3 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "EE-3 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

HDR="$(adb exec-out run-as "$PKG" head -n1 "$CSV" 2>/dev/null | tr -d '\r')"
[ "$HDR" = "$EXP" ] || { echo "EE-3 RESULT=FAIL (bad or missing header)" | tee "$OUT"; exit 4; }

echo "EE-3 RESULT=PASS" | tee "$OUT"
exit 0
