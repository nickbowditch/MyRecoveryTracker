#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
CSV="files/daily_notification_latency.csv"
LOCK="app/locks/daily_notif_latency.header"
OUT="evidence/v6.0/notification_latency/di2.1.txt"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "DI-2 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "DI-2 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

EXP="$(tr -d '\r' < "$LOCK" 2>/dev/null || true)"
[ -n "$EXP" ] || { echo "DI-2 RESULT=FAIL (missing lock)" | tee "$OUT"; exit 4; }

HDR="$(adb exec-out run-as "$PKG" head -n1 "$CSV" 2>/dev/null | tr -d '\r' || true)"
[ -n "$HDR" ] || { echo "DI-2 RESULT=FAIL (missing csv)" | tee "$OUT"; exit 5; }
[ "$HDR" = "$EXP" ] || { echo "DI-2 RESULT=FAIL (bad header)" | tee "$OUT"; exit 6; }

DUPS="$(adb exec-out run-as "$PKG" sh -c 'awk -F, "NR>1{gsub(/^[[:space:]]+|[[:space:]]+$/, \"\", \$1); c[\$1]++} END{n=0; for(k in c) if(c[k]>1) n+=c[k]-1; print n}" "'"$CSV"'"' 2>/dev/null | tr -d '\r' || true)"
[ -n "$DUPS" ] || DUPS=0

[ "$DUPS" -eq 0 ] || { echo "DI-2 RESULT=FAIL (duplicate dates: $DUPS)" | tee "$OUT"; exit 7; }

echo "DI-2 RESULT=PASS" | tee "$OUT"
exit 0
