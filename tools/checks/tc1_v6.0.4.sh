#!/bin/sh
PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/unlocks/tc1.4.txt"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "TC-1 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "TC-1 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

CSV="$(adb exec-out run-as "$PKG" cat files/daily_unlocks.csv 2>/dev/null | tr -d '\r')"
[ -n "$CSV" ] || { echo "TC-1 RESULT=FAIL (missing csv)" | tee "$OUT"; exit 4; }

HEAD="$(printf '%s\n' "$CSV" | head -n1)"
printf '%s\n' "$HEAD" | grep -q '^date,' || { echo "TC-1 RESULT=FAIL (bad header)" | tee "$OUT"; exit 5; }

D_LOCAL="$(adb shell date +%F 2>/dev/null | tr -d '\r')"
D_UTC="$(adb shell TZ=UTC date +%F 2>/dev/null | tr -d '\r')"
EPOCH="$(adb shell toybox date +%s 2>/dev/null | tr -d '\r')"
Y_LOCAL="$(adb shell toybox date -d "@$((EPOCH-86400))" +%F 2>/dev/null | tr -d '\r')"

ok_today="$(printf '%s\n' "$CSV" | awk -F, -v a="$D_LOCAL" -v b="$D_UTC" 'NR>1&&($1==a||$1==b){f=1;exit} END{print f?1:0}')"
ok_yday="$(printf '%s\n' "$CSV" | awk -F, -v y="$Y_LOCAL" 'NR>1&&$1==y{f=1;exit} END{print f?1:0}')"
future_bad="$(printf '%s\n' "$CSV" | awk -F, -v a="$D_LOCAL" -v b="$D_UTC" 'BEGIN{m=a; if(b>m)m=b} NR>1 && $1>m{print;exit}')"

if [ "$ok_today" -eq 1 ] || { [ "$ok_yday" -eq 1 ] && [ -z "$future_bad" ]; }; then
echo "TC-1 RESULT=PASS" | tee "$OUT"
exit 0
else
echo "TC-1 RESULT=FAIL" | tee "$OUT"
exit 1
fi
