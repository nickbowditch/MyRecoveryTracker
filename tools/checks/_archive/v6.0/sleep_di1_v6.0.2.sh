#!/bin/bash
PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/sleep/di1.2.txt"

adb get-state >/dev/null 2>&1 || { echo "DI-1 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "DI-1 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

LOCK="$(tr -d $'\r' < app/locks/daily_sleep.header 2>/dev/null)"
[ -n "$LOCK" ] || { echo "DI-1 RESULT=FAIL (missing lock header)" | tee "$OUT"; exit 4; }

HEAD="$(adb exec-out run-as "$PKG" sh -c 'head -n 1 files/daily_sleep.csv 2>/dev/null' | tr -d $'\r')"
[ -n "$HEAD" ] || { echo "DI-1 RESULT=FAIL (missing csv)" | tee "$OUT"; exit 5; }

if [ "$HEAD" = "$LOCK" ]; then
  echo "DI-1 RESULT=PASS" | tee "$OUT"
  exit 0
else
  {
    echo "DI-1 RESULT=FAIL"
    echo "LOCK=$LOCK"
    echo "HEAD=$HEAD"
  } | tee "$OUT"
  exit 1
fi
