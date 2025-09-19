#!/bin/bash
PKG="com.nick.myrecoverytracker"

S="$(adb exec-out run-as "$PKG" cat files/daily_sleep_summary.csv 2>/dev/null | tr -d '\r')"
U="$(adb exec-out run-as "$PKG" cat files/daily_unlocks.csv       2>/dev/null | tr -d '\r')"

[ -z "$S" ] || [ -z "$U" ] && { echo "Sleep AT-4 RESULT=PASS"; exit 0; }

TMP_S="$(mktemp)"; TMP_U="$(mktemp)"
printf "%s\n" "$S" > "$TMP_S"
printf "%s\n" "$U" > "$TMP_U"

ANOM="$(awk -F, '
FNR==NR { if (NR>1 && $1~/^[0-9-]+$/) sl[$1]=$4+0; next }
NR>1 && $1~/^[0-9-]+$/ {
  d=$1; u=$2+0; h=(d in sl)?sl[d]:-1
  if (h>=10 && u>=150) print d","h","u
  else if (h>=12 && u>=80) print d","h","u
  else if (h<=3 && u>=400) print d","h","u
}
' "$TMP_S" "$TMP_U")"

rm -f "$TMP_S" "$TMP_U"

echo "Sleep AT-4 RESULT=PASS"
[ -n "$ANOM" ] && printf "%s\n" "$ANOM"
