#!/bin/sh
PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/sleep/tc3.2.txt"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { printf '%s\n' "TC-3 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { printf '%s\n' "TC-3 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

TODAY="$(adb shell date +%F 2>/dev/null | tr -d '\r')"
CSV1="$(adb exec-out run-as "$PKG" cat files/daily_sleep.csv 2>/dev/null | tr -d '\r')"
CSV2="$(adb exec-out run-as "$PKG" cat files/daily_sleep_duration.csv 2>/dev/null | tr -d '\r')"
[ -n "$CSV1" ] || { printf '%s\n' "TC-3 RESULT=FAIL (missing daily_sleep.csv)" | tee "$OUT"; exit 4; }
[ -n "$CSV2" ] || { printf '%s\n' "TC-3 RESULT=FAIL (missing daily_sleep_duration.csv)" | tee "$OUT"; exit 5; }

BAD="$(awk -F',' -v t="$TODAY" '
FNR==1{for(i=1;i<=NF;i++) if($i=="date") di=i; next}
FNR>1{ if($di>t) print FILENAME ":" $0 }
' /dev/stdin /dev/stdin <<<"$CSV1" <<<"$CSV2")"

if [ -z "$BAD" ]; then
printf '%s\n' "TC-3 RESULT=PASS" | tee "$OUT"
exit 0
else
{
printf '%s\n' "TC-3 RESULT=FAIL"
printf '%s\n' "$BAD"
} | tee "$OUT"
exit 1
fi
