#!/bin/sh
PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/lnsu/raw_header.1.txt"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "LNSU-RAW-HEADER RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "LNSU-RAW-HEADER RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

HDR="$(adb exec-out run-as "$PKG" head -n1 files/screen_log.csv 2>/dev/null | tr -d '\r')"
[ -n "$HDR" ] || { echo "LNSU-RAW-HEADER RESULT=FAIL (missing files/screen_log.csv)" | tee "$OUT"; exit 4; }

{ echo "HEADER:$HDR"; echo "LNSU-RAW-HEADER RESULT=PASS"; } | tee "$OUT"
exit 0
