#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
CSV="files/daily_unlocks.csv"
OUT="evidence/v6.0/unlocks/di2.2.txt"
mkdir -p "$(dirname "$OUT")"
adb get-state >/dev/null 2>&1 || { echo "DI-2 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "DI-2 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }
DATA="$(adb exec-out run-as "$PKG" cat "$CSV" 2>/dev/null | tr -d '\r' || true)"
[ -n "$DATA" ] || { echo "DI-2 RESULT=FAIL (missing csv)" | tee "$OUT"; exit 4; }
DUPS="$(printf '%s\n' "$DATA" | awk -F',' 'NR>1{c[$1]++} END{for(k in c) if(c[k]>1) print k":"c[k]}' || true)"
[ -z "$DUPS" ] || { { echo "DI-2 RESULT=FAIL (duplicate dates)"; printf '%s\n' "$DUPS"; } | tee "$OUT"; exit 1; }
echo "DI-2 RESULT=PASS" | tee "$OUT"
exit 0
