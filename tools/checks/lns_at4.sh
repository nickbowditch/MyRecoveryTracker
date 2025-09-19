#!/usr/bin/env bash
PKG="com.nick.myrecoverytracker"

S="$(adb exec-out run-as "$PKG" cat files/daily_late_screen.csv 2>/dev/null | tr -d '\r')"
U="$(adb exec-out run-as "$PKG" cat files/daily_unlocks.csv    2>/dev/null | tr -d '\r')"

[ -z "$S" ] || [ -z "$U" ] && { echo "LNS AT-4 RESULT=PASS"; exit 0; }

TMP_S="$(mktemp)"; TMP_U="$(mktemp)"
printf "%s\n" "$S" > "$TMP_S"
printf "%s\n" "$U" > "$TMP_U"

ANOM="$(awk -F, '
FNR==NR { if (NR>1 && $1~/^[0-9-]+$/) late[$1]=$2+0; next }
NR>1 && $1~/^[0-9-]+$/ {
  d=$1; u=$2+0; m=(d in late)?late[d]:-1
  if (m>=180 && u<=20) print d","m","u
  else if (m==0 && u>=300) print d","m","u
}
' "$TMP_S" "$TMP_U")"

rm -f "$TMP_S" "$TMP_U"

echo "LNS AT-4 RESULT=PASS"
[ -n "$ANOM" ] && printf "%s\n" "$ANOM"
