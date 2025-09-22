#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/lnsu/di2.2.txt"
CSV="files/daily_lnsu.csv"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "DI-2 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "DI-2 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

OWN_UID="$(adb exec-out run-as "$PKG" sh -c 'ls -n "'"$CSV"'" 2>/dev/null | awk "{print \$3}"' | tr -d '\r' || true)"
[ -n "$OWN_UID" ] || { echo "DI-2 RESULT=FAIL (missing csv)" | tee "$OUT"; exit 4; }
PKG_UID="$(adb exec-out run-as "$PKG" id -u 2>/dev/null | tr -d '\r' || true)"
[ -n "$PKG_UID" ] || { echo "DI-2 RESULT=FAIL (uid unreadable)" | tee "$OUT"; exit 5; }
[ "$OWN_UID" = "$PKG_UID" ] || { echo "DI-2 RESULT=FAIL (bad owner $OWN_UID!=${PKG_UID})" | tee "$OUT"; exit 6; }

JOBS="$(adb shell dumpsys jobscheduler "$PKG" 2>/dev/null | grep -i 'LnsuRollupWorker' || true)"
[ -n "$JOBS" ] || { echo "DI-2 RESULT=FAIL (no LnsuRollupWorker scheduled)" | tee "$OUT"; exit 7; }

echo "DI-2 RESULT=PASS" | tee "$OUT"
exit 0
