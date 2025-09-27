#!/bin/sh
set -eu
APP="com.nick.myrecoverytracker"
RAW="files/notification_log.csv"
DAILY="files/daily_notification_engagement.csv"
OUT="evidence/v6.0/notification_engagement/at2.4.txt"
mkdir -p "$(dirname "$OUT")"
fail(){ echo "AT2 RESULT=FAIL $1" | tee "$OUT"; exit 1; }

adb get-state >/dev/null 2>&1 || fail "(no device)"
adb shell pm path "$APP" >/dev/null 2>&1 || fail "(app not installed)"

SNAP="$(adb exec-out run-as "$APP" sh -c '
stable(){
  f="$1"; i=0
  while :; do
    [ -f "$f" ] || { echo "MISS"; return 1; }
    a="$(cksum "$f" 2>/dev/null)"; s="$(wc -c <"$f" 2>/dev/null)"
    sleep 0.25
    a2="$(cksum "$f" 2>/dev/null)"; s2="$(wc -c <"$f" 2>/dev/null)"
    [ "$a" = "$a2" ] && [ "$s" = "$s2" ] && break
    i=$((i+1)); [ $i -ge 20 ] && break
  done
}
stable "'"$RAW"'"
stable "'"$DAILY"'"
echo "-----RAW-----"
[ -f "'"$RAW"'" ] && cat "'"$RAW"'" || echo "[MISSING: '"$RAW"']"
echo "-----DAILY-----"
[ -f "'"$DAILY"'" ] && cat "'"$DAILY"'" || echo "[MISSING: '"$DAILY"']"
' | tr -d '\r' || true)"

RAW_DATA="$(printf '%s\n' "$SNAP" | awk '/^-----RAW-----/{flag=1;next}/^-----DAILY-----/{flag=0}flag')"
DAILY_DATA="$(printf '%s\n' "$SNAP" | awk '/^-----DAILY-----/{flag=1;next}flag')"

[ -n "$RAW_DATA" ] || fail "(missing raw)"
[ -n "$DAILY_DATA" ] || fail "(missing daily)"

dates="$(printf '%s\n' "$DAILY_DATA" | awk -F, 'NR>1 && $1 ~ /^[0-9]{4}-[0-9]{2}-[0-9]{2}$/ {print $1}' | sort | uniq)"
[ -n "$dates" ] || fail "(no dates in daily)"

check_date() {
d="$1"
dd="$(printf '%s\n' "$DAILY_DATA" | awk -F, -v d="$d" 'NR>1 && $1==d {print $3","$4; exit}')"
[ -n "$dd" ] || { echo "MISS $d (no daily row)"; return; }
dd_del="$(printf '%s' "$dd" | cut -d, -f1)"
dd_op="$(printf '%s' "$dd" | cut -d, -f2)"
rd="$(printf '%s\n' "$RAW_DATA" | awk -F, -v d="$d" '
BEGIN{lc=0; oc=0}
NR==1{
  for(i=1;i<=NF;i++){
    n=$i; gsub(/\r/,"",n)
    if(n=="ts"||n=="timestamp") tsi=i
    if(n=="event") ei=i
  }
  next
}
{
  t=$tsi; e=$ei
  if(length(t)>=10 && substr(t,1,10)==d){
    if(e=="POSTED"||e=="posted") lc++
    if(e=="CLICKED"||e=="clicked") oc++
  }
}
END{print lc "," oc}
')"
[ -n "$rd" ] || rd="0,0"
rd_del="$(printf '%s' "$rd" | cut -d, -f1)"
rd_op="$(printf '%s' "$rd" | cut -d, -f2)"
diff_del=$(( rd_del - dd_del )); [ $diff_del -lt 0 ] && diff_del=$(( -diff_del ))
diff_op=$(( rd_op - dd_op )); [ $diff_op -lt 0 ] && diff_op=$(( -diff_op ))
if [ "$diff_del" -le 1 ] && [ "$diff_op" -le 1 ]; then
  echo "OK $d raw=${rd_del},${rd_op} daily=${dd_del},${dd_op}"
else
  echo "BAD $d raw=${rd_del},${rd_op} daily=${dd_del},${dd_op}"
fi
}

RESULTS=""
while IFS= read -r d; do
  [ -n "$d" ] || continue
  line="$(check_date "$d")"
  RESULTS="${RESULTS}${line}\n"
done <<EOF_DATES
$dates
EOF_DATES

printf "%b" "$RESULTS" | grep -q '^BAD ' && { printf "%b" "$RESULTS" | tee "$OUT" >/dev/null; echo "AT2 RESULT=FAIL" | tee -a "$OUT"; exit 1; }
printf "%b" "$RESULTS" | tee "$OUT" >/dev/null
echo "AT2 RESULT=PASS" | tee -a "$OUT"
exit 0
