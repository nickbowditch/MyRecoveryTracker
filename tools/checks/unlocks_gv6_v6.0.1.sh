#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
CSV="files/daily_unlocks.csv"
LOCK="app/locks/daily_unlocks.header"
OUT="evidence/v6.0/unlocks/gv6.1.txt"
mkdir -p "$(dirname "$OUT")"
adb get-state >/dev/null 2>&1 || { echo "GV-6 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG"  >/dev/null 2>&1 || { echo "GV-6 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }
EXP="$(tr -d '\r' < "$LOCK" 2>/dev/null || true)"
[ -n "$EXP" ] || { echo "GV-6 RESULT=FAIL (missing lock)" | tee "$OUT"; exit 4; }
DATA="$(adb exec-out run-as "$PKG" cat "$CSV" 2>/dev/null | tr -d '\r' || true)"
[ -n "$DATA" ] || { echo "GV-6 RESULT=FAIL (missing csv)" | tee "$OUT"; exit 5; }
printf '%s\n' "$DATA" | awk -F',' -v exp="$EXP" '
NR==1{
  if($0!=exp) exit 6
  for(i=1;i<=NF;i++){
    if($i=="feature_schema_version") sv=i
    if($i=="daily_unlocks") du=i
  }
  if(!sv||!du) exit 7
  next
}
NR>1{
  if($sv!="v6.0") exit 8
  v=$du
  if(v !~ /^[0-9]+$/) exit 9
  if((v+0)<0 || (v+0)>20000) exit 9
}
END{}
' || { rc=$?; case "$rc" in
  6) echo "GV-6 RESULT=FAIL (header drift vs lock)" | tee "$OUT" ;;
  7) echo "GV-6 RESULT=FAIL (required columns missing)" | tee "$OUT" ;;
  8) echo "GV-6 RESULT=FAIL (schema version not v6.0)" | tee "$OUT" ;;
  9) echo "GV-6 RESULT=FAIL (daily_unlocks type/range)" | tee "$OUT" ;;
  *) echo "GV-6 RESULT=FAIL (unknown)" | tee "$OUT" ;;
esac; exit 1; }
echo "GV-6 RESULT=PASS" | tee "$OUT"
exit 0
