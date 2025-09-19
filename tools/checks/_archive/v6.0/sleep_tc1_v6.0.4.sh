#!/bin/bash
PKG="com.nick.myrecoverytracker"
adb get-state >/dev/null 2>&1 || { echo "TC-1 RESULT=FAIL (no device)"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "TC-1 RESULT=FAIL (app not installed)"; exit 3; }

CSV="$(adb exec-out run-as "$PKG" cat files/daily_sleep.csv 2>/dev/null || printf "")"
[ -n "$CSV" ] || { echo "TC-1 RESULT=FAIL"; exit 1; }
HEAD="$(printf '%s\n' "$CSV" | head -n1 | tr -d $'\r')"
printf '%s\n' "$HEAD" | grep -Eq '^date,.*(sleep|wake|duration)' || { echo "TC-1 RESULT=FAIL"; exit 1; }

D_LOCAL="$(adb shell date +%F | tr -d $'\r')"
D_UTC="$(adb shell TZ=UTC date +%F | tr -d $'\r')"

has_row(){ awk -F, -v d="$1" 'NR>1&&$1==d{print 1;exit} END{print 0}'; }
CL="$(printf '%s\n' "$CSV" | has_row "$D_LOCAL")"
CU="$(printf '%s\n' "$CSV" | has_row "$D_UTC")"

if [ "$D_LOCAL" = "$D_UTC" ]; then
  [ "$CL" -eq 1 -o "$CU" -eq 1 ] && { echo "TC-1 RESULT=PASS"; exit 0; }
  echo "TC-1 RESULT=FAIL"; exit 1
else
  [ "$CL" -eq 1 ] && [ "$CU" -eq 0 ] && { echo "TC-1 RESULT=PASS"; exit 0; }
  echo "TC-1 RESULT=FAIL"; exit 1
fi
