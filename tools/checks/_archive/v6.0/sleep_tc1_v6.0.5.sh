#!/bin/bash
PKG="com.nick.myrecoverytracker"
adb get-state >/dev/null 2>&1 || { echo "TC-1 RESULT=FAIL (no device)"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "TC-1 RESULT=FAIL (app not installed)"; exit 3; }

CSV="$(adb exec-out run-as "$PKG" cat files/daily_sleep.csv 2>/dev/null || printf "")"
[ -n "$CSV" ] || { echo "TC-1 RESULT=FAIL"; exit 1; }
HEAD="$(printf '%s\n' "$CSV" | head -n1 | tr -d $'\r')"
printf '%s\n' "$HEAD" | grep -Eq '^date,' || { echo "TC-1 RESULT=FAIL"; exit 1; }
printf '%s\n' "$HEAD" | grep -Eq '(duration|minutes)' || { echo "TC-1 RESULT=FAIL"; exit 1; }

TODAY="$(adb shell date +%F | tr -d $'\r')"
printf '%s\n' "$CSV" | awk -F, -v t="$TODAY" 'NR>1 && $1>t{bad=1} END{exit bad?1:0}' || { echo "TC-1 RESULT=FAIL"; exit 1; }

echo "TC-1 RESULT=PASS"
exit 0
