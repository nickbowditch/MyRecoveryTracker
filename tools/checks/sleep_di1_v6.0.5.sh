#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/sleep/di1.5.txt"
SUM="files/daily_sleep_summary.csv"
DUR="files/daily_sleep_duration.csv"
LSUM="app/locks/daily_sleep_summary.header"
LDUR="app/locks/daily_sleep_duration.header"
mkdir -p "$(dirname "$OUT")"
adb get-state >/dev/null 2>&1 || { echo "DI-1 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG"  >/dev/null 2>&1 || { echo "DI-1 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }
ESUM="$(tr -d '\r' < "$LSUM" 2>/dev/null || true)"
EDUR="$(tr -d '\r' < "$LDUR" 2>/dev/null || true)"
[ -n "$ESUM" ] || { echo "DI-1 RESULT=FAIL (missing summary lock)" | tee "$OUT"; exit 4; }
[ -n "$EDUR" ] || { echo "DI-1 RESULT=FAIL (missing duration lock)" | tee "$OUT"; exit 4; }
HSUM="$(adb exec-out run-as "$PKG" head -n1 "$SUM" 2>/dev/null | tr -d '\r' || true)"
HDUR="$(adb exec-out run-as "$PKG" head -n1 "$DUR" 2>/dev/null | tr -d '\r' || true)"
[ -n "$HSUM" ] || { echo "DI-1 RESULT=FAIL (missing summary csv)" | tee "$OUT"; exit 5; }
[ -n "$HDUR" ] || { echo "DI-1 RESULT=FAIL (missing duration csv)" | tee "$OUT"; exit 5; }
[ "$HSUM" = "$ESUM" ] || { echo "DI-1 RESULT=FAIL (summary header drift)" | tee "$OUT"; exit 6; }
[ "$HDUR" = "$EDUR" ] || { echo "DI-1 RESULT=FAIL (duration header drift)" | tee "$OUT"; exit 6; }
echo "DI-1 RESULT=PASS" | tee "$OUT"
exit 0
