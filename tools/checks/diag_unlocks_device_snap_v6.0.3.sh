#!/bin/bash
PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/_repo/unlocks_device_snap.3.txt"
set -euo pipefail
adb get-state >/dev/null 2>&1 || { echo "DIAG-SNAP RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "DIAG-SNAP RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }
{
  adb exec-out run-as "$PKG" sh -c 'head -n 1 files/daily_unlocks.csv 2>/dev/null || true' | tr -d $'\r'
  adb exec-out run-as "$PKG" sh -c 'tail -n 5 files/daily_unlocks.csv 2>/dev/null || true'
  adb exec-out run-as "$PKG" sh -c 'ls -l --time-style=full-iso files/daily_unlocks.csv* 2>/dev/null || true'
} | tee "$OUT"
echo "DIAG-SNAP RESULT=PASS" | tee -a "$OUT"
