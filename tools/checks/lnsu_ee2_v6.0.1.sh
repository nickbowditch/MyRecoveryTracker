#!/bin/sh
PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/lnsu/ee2.1.txt"
RAW="files/screen_log.csv"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "EE-2 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "EE-2 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

adb exec-out run-as "$PKG" ls "$RAW" >/dev/null 2>&1 || { echo "EE-2 RESULT=FAIL (missing $RAW)" | tee "$OUT"; exit 4; }

echo "EE-2 RESULT=PASS" | tee "$OUT"
exit 0
