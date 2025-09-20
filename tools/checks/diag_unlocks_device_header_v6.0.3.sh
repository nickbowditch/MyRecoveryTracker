#!/bin/bash
PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/_repo/unlocks_device_header.3.txt"
set -euo pipefail
adb get-state >/dev/null 2>&1 || { echo "DIAG-DEVICE RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "DIAG-DEVICE RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }
LOCK="$(tr -d $'\r' < app/locks/daily_metrics.header 2>/dev/null || true)"
HEAD="$(adb exec-out run-as "$PKG" sh -c 'head -n 1 files/daily_unlocks.csv 2>/dev/null' | tr -d $'\r' || true)"
STAT="$(adb exec-out run-as "$PKG" sh -c 'ls -l --time-style=full-iso files/daily_unlocks.csv 2>/dev/null' || true)"
{
  echo "LOCK:$LOCK"
  echo "HEAD:$HEAD"
  echo "STAT:$STAT"
} | tee "$OUT"
if [ -z "$HEAD" ]; then echo "DIAG-DEVICE RESULT=FAIL (missing csv)" | tee -a "$OUT"; exit 4; fi
if [ "$HEAD" != "$LOCK" ]; then echo "DIAG-DEVICE RESULT=FAIL (header drift)" | tee -a "$OUT"; exit 1; fi
echo "DIAG-DEVICE RESULT=PASS" | tee -a "$OUT"
