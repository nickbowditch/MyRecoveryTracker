#!/bin/sh
PKG="com.nick.myrecoverytracker"

CSV="$(adb exec-out run-as "$PKG" cat files/daily_sleep.csv 2>/dev/null || printf "")"
[ -n "$CSV" ] || { echo "TC-5 RESULT=FAIL (missing csv)"; exit 2; }

GAP=""
prev=""

tmp="$(mktemp)"
printf '%s\n' "$CSV" > "$tmp"

while IFS=, read -r d rest; do
[ "$d" = "date" ] && continue
if [ -n "$prev" ]; then
p=$(date -j -f "%Y-%m-%d" "$prev" +%s 2>/dev/null || echo 0)
s=$(date -j -f "%Y-%m-%d" "$d" +%s 2>/dev/null || echo 0)
if [ "$p" -gt 0 ] && [ "$s" -gt 0 ]; then
diff=$(( (s - p) / 86400 ))
if [ "$diff" -gt 2 ]; then
GAP="$prev->$d (${diff} days)"
break
fi
fi
fi
prev="$d"
done < "$tmp"
rm -f "$tmp"

if [ -z "$GAP" ]; then
echo "TC-5 RESULT=PASS"
exit 0
else
echo "TC-5 RESULT=FAIL"
echo "$GAP"
exit 1
fi
