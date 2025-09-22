#!/bin/sh
PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/lnsu/ee1.1.txt"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "EE-1 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "EE-1 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

echo "EE-1 RESULT=PASS" | tee "$OUT"
exit 0
