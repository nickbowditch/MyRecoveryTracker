#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/sleep/di2.2.txt"
SUM="files/daily_sleep_summary.csv"
DUR="files/daily_sleep_duration.csv"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "DI-2 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG"  >/dev/null 2>&1 || { echo "DI-2 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

S_SUM="$(adb exec-out run-as "$PKG" tail -n +2 "$SUM" 2>/dev/null | awk -F, 'NF{print $1}' | sort | uniq -d || true)"
S_DUR="$(adb exec-out run-as "$PKG" tail -n +2 "$DUR" 2>/dev/null | awk -F, 'NF{print $1}' | sort | uniq -d || true)"

[ -z "$S_SUM$S_DUR" ] || { echo "DI-2 RESULT=FAIL (duplicate dates)"; printf '%s\n' "$S_SUM" "$S_DUR" | tee -a "$OUT"; exit 1; }

echo "DI-2 RESULT=PASS" | tee "$OUT"
exit 0
