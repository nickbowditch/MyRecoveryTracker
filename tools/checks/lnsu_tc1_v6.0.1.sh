#!/bin/sh
PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/lnsu/tc1.1.txt"
CSV="files/daily_lnsu.csv"
LOCK="app/locks/daily_lnsu.header"
mkdir -p "$(dirname "$OUT")"
adb get-state >/dev/null 2>&1 || { echo "TC-1 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "TC-1 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }
HDR="$(tr -d '\r' < "$LOCK" 2>/dev/null)"
[ -n "$HDR" ] || { echo "TC-1 RESULT=FAIL (missing lock)" | tee "$OUT"; exit 4; }
CSVH="$(adb exec-out run-as "$PKG" head -n1 "$CSV" 2>/dev/null | tr -d '\r')"
[ "$CSVH" = "$HDR" ] || { echo "TC-1 RESULT=FAIL (bad header)" | tee "$OUT"; exit 5; }
TODAY="$(adb shell date +%F 2>/dev/null | tr -d '\r')"
adb exec-out run-as "$PKG" awk -F, -v t="$TODAY" 'NR>1 && $1>t{e=1} END{exit e?1:0}' "$CSV"
[ $? -eq 0 ] || { echo "TC-1 RESULT=FAIL (future-dated rows)" | tee "$OUT"; exit 6; }
echo "TC-1 RESULT=PASS" | tee "$OUT"; exit 0
