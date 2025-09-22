#!/bin/sh
PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/unlocks/at2.console.2.txt"

seldev(){ for i in $(seq 1 15); do d=$(adb devices | awk 'NR>1 && $2=="device"{print $1;exit}'); [ -n "$d" ] && { echo "$d"; return; } ; sleep 1; done; }
DEV=$(seldev) || { echo "AT-2 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }

adb -s "$DEV" shell pm path "$PKG" >/dev/null 2>&1 || { echo "AT-2 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

CSV_A="$(adb -s "$DEV" exec-out run-as "$PKG" cat files/daily_unlocks.csv 2>/dev/null)"
CSV_B="$(adb -s "$DEV" exec-out run-as "$PKG" cat files/unlock_log.csv 2>/dev/null)"
[ -n "$CSV_A" ] && [ -n "$CSV_B" ] || { echo "AT-2 RESULT=FAIL (missing csv)" | tee "$OUT"; exit 4; }

echo "AT-2 RESULT=PASS" | tee "$OUT"; exit 0
