#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
CSV="files/daily_app_switching.csv"
OUT="evidence/v6.0/app_switching/tc5.txt"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "TC-5 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "TC-5 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

HDR="$(adb exec-out run-as "$PKG" head -n1 "$CSV" 2>/dev/null | tr -d '\r' || true)"
[ -n "$HDR" ] || { echo "TC-5 RESULT=FAIL (missing csv)" | tee "$OUT"; exit 4; }
[ "$HDR" = "date,switches,entropy" ] || { echo "TC-5 RESULT=FAIL (bad header)" | tee "$OUT"; exit 5; }

R="$(adb exec-out run-as "$PKG" sh -c '
set -eu
f="'"$CSV"'"
[ -f "$f" ] || { echo NOCSV; exit 0; }
tail -n +2 "$f" 2>/dev/null \
| awk -F, "NF>=1{print \$1}" \
| toybox tr -d "\r" \
| toybox sort \
| toybox uniq \
| {
prev=""; preve=""; maxgap=0; maxpair=""
while IFS= read -r d; do
  [ -z "$d" ] && continue
  e=$(toybox date -d "$d 00:00:00" +%s 2>/dev/null) || continue
  if [ -n "$preve" ]; then
    diff=$(( (e - preve) / 86400 ))
    if [ "$diff" -gt "$maxgap" ]; then
      maxgap="$diff"
      maxpair="$prev->$d"
    fi
    if [ "$diff" -gt 2 ]; then
      echo "GAP $diff prev=$prev next=$d MAXGAP=$maxgap PAIR=$maxpair"
      exit 0
    fi
  fi
  prev="$d"; preve="$e"
done
echo "OK MAXGAP=$maxgap PAIR=$maxpair"
}
')"

case "$R" in
OK*)   echo "TC-5 RESULT=PASS ($R)" | tee "$OUT"; exit 0 ;;
NOCSV) echo "TC-5 RESULT=FAIL (missing csv)" | tee "$OUT"; exit 7 ;;
GAP*)  echo "TC-5 RESULT=FAIL ($R)" | tee "$OUT"; exit 1 ;;
*)     echo "TC-5 RESULT=FAIL (unknown)" | tee "$OUT"; exit 8 ;;
esac
