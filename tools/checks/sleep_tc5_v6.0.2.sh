#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/sleep/tc5.2.txt"
DUR="files/daily_sleep_duration.csv"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "TC-5 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "TC-5 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

DATES="$(adb exec-out run-as "$PKG" tail -n +2 "$DUR" 2>/dev/null | tr -d '\r' | awk -F',' 'NF{print $1}' | sort -u || true)"
[ -n "$DATES" ] || { echo "TC-5 RESULT=FAIL (missing/empty daily_sleep_duration.csv)" | tee "$OUT"; exit 4; }

MIN="$(printf '%s\n' "$DATES" | head -n1)"
MAX="$(printf '%s\n' "$DATES" | tail -n1)"

cur="$MIN"
miss=0
while [ "$cur" != "$MAX" ]; do
if ! printf '%s\n' "$DATES" | grep -qx "$cur"; then
miss=$((miss+1))
if [ $miss -gt 2 ]; then
echo "TC-5 RESULT=FAIL (gap >2 days near $cur)" | tee "$OUT"
exit 1
fi
else
miss=0
fi
cur="$(date -j -f %F -v+1d "$cur" +%F 2>/dev/null)"
[ -n "$cur" ] || { echo "TC-5 RESULT=FAIL (host date error)" | tee "$OUT"; exit 5; }
done

echo "TC-5 RESULT=PASS" | tee "$OUT"
exit 0
