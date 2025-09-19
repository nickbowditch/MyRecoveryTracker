#!/bin/bash
PKG="com.nick.myrecoverytracker"

adb get-state >/dev/null 2>&1 || { echo "TC-1 RESULT=FAIL (no device)"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "TC-1 RESULT=FAIL (app not installed)"; exit 3; }

D_LOCAL="$(adb shell date +%F | tr -d $'\r')"
D_UTC="$(adb shell TZ=UTC date +%F | tr -d $'\r')"
CSV="$(adb exec-out run-as "$PKG" cat files/daily_sleep.csv 2>/dev/null || printf "")"

has_row(){ awk -F, -v d="$1" 'NR>1&&$1==d{f=1;exit} END{print f?1:0}'; }

CL="$(printf '%s\n' "$CSV" | has_row "$D_LOCAL")"
CU="$(printf '%s\n' "$CSV" | has_row "$D_UTC")"

if [ "$D_LOCAL" = "$D_UTC" ]; then
  [ "$CL" -eq 1 ] && { echo "TC-1 RESULT=PASS"; exit 0; }
else
  [ "$CL" -eq 1 ] && [ "$CU" -eq 0 ] && { echo "TC-1 RESULT=PASS"; exit 0; }
fi

echo "TC-1 RESULT=FAIL"
printf "%s\n" "$CSV" | tail -n 8
exit 1
