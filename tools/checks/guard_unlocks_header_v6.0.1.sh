#!/bin/bash
PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/_repo/unlocks_guard.1.txt"
set -euo pipefail
adb get-state >/dev/null 2>&1 || { echo "UNLOCKS-GUARD RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "UNLOCKS-GUARD RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }
HEAD="$(adb exec-out run-as "$PKG" sh -c 'head -n 1 files/daily_unlocks.csv 2>/dev/null' | tr -d $'\r' || true)"
LOCK="$(tr -d $'\r' < app/locks/daily_metrics.header 2>/dev/null || true)"
{
  echo "HEAD:$HEAD"
  echo "LOCK:$LOCK"
} | tee "$OUT"
if [ -z "$HEAD" ]; then echo "UNLOCKS-GUARD RESULT=FAIL (missing csv)" | tee -a "$OUT"; exit 4; fi
if [ "$HEAD" != "$LOCK" ]; then echo "UNLOCKS-GUARD RESULT=FAIL (header drift)" | tee -a "$OUT"; exit 1; fi
echo "UNLOCKS-GUARD RESULT=PASS" | tee -a "$OUT"
