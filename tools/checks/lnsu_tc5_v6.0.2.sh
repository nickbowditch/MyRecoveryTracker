#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/lnsu/tc5.2.txt"
CSV="files/daily_lnslu.csv"
LOCK="app/locks/daily_lnslu.header"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "TC-5 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "TC-5 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

EXP="$(tr -d '\r' < "$LOCK" 2>/dev/null || true)"
[ -n "$EXP" ] || { echo "TC-5 RESULT=FAIL (missing lock)" | tee "$OUT"; exit 4; }

HDR="$(adb exec-out run-as "$PKG" head -n1 "$CSV" 2>/dev/null | tr -d '\r' || true)"
[ -n "$HDR" ] || { echo "TC-5 RESULT=FAIL (missing csv)" | tee "$OUT"; exit 5; }
[ "$HDR" = "$EXP" ] || { echo "TC-5 RESULT=FAIL (bad header)" | tee "$OUT"; exit 6; }

R="$(adb exec-out run-as "$PKG" sh -c '
set -eu
f="'"$CSV"'"
[ -f "$f" ] || { echo NOCSV; exit 0; }
tail -n +2 "$f" | toybox cut -d, -f1 | toybox sort | toybox uniq | {
prev=""; preve="";
while IFS= read -r d; do
e=$(toybox date -d "$d 00:00:00" +%s 2>/dev/null) || continue
if [ -n "$preve" ]; then
diff=$(( (e - preve) / 86400 ))
if [ "$diff" -gt 2 ]; then
echo "GAP $diff prev=$prev next=$d"
exit 0
fi
fi
prev="$d"; preve="$e"
done
echo OK
}
')"

case "$R" in
OK)    echo "TC-5 RESULT=PASS" | tee "$OUT"; exit 0 ;;
NOCSV) echo "TC-5 RESULT=FAIL (missing csv)" | tee "$OUT"; exit 7 ;;
GAP*)  echo "TC-5 RESULT=FAIL ($R)" | tee "$OUT"; exit 1 ;;
*)     echo "TC-5 RESULT=FAIL (unknown)" | tee "$OUT"; exit 8 ;;
esac
