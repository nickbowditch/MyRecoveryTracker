#!/bin/sh
set -eu
APP="com.nick.myrecoverytracker"
RAW="files/notification_log.csv"
DAILY="files/daily_notification_engagement.csv"
OUT="evidence/v6.0/notification_engagement/at2.5.txt"

fail(){ echo "AT2 RESULT=FAIL $1" | tee "$OUT"; exit 1; }

adb get-state >/dev/null 2>&1 || fail "(no device)"
adb shell pm path "$APP" >/dev/null 2>&1 || fail "(app not installed)"

RAW_HDR="$(adb exec-out run-as "$APP" sed -n '1p' "$RAW" 2>/dev/null | tr -d '\r' || true)"
DAILY_HDR="$(adb exec-out run-as "$APP" sed -n '1p' "$DAILY" 2>/dev/null | tr -d '\r' || true)"
[ -n "$RAW_HDR" ] || fail "(missing raw)"
[ -n "$DAILY_HDR" ] || fail "(missing daily)"

RAW_DATES="$(adb exec-out run-as "$APP" awk -F, '
NR==1{
for(i=1;i<=NF;i++){
h=$i; gsub(/\r/,"",h)
if(h=="ts"||h=="timestamp") tsi=i
}
next
}
{
t=$tsi; if(length(t)>=10) print substr(t,1,10)
}' "$RAW" 2>/dev/null | sort -u || true)"

DAILY_DATES="$(adb exec-out run-as "$APP" awk -F, '
NR>1 && $1 ~ /^[0-9]{4}-[0-9]{2}-[0-9]{2}$/ {print $1}
' "$DAILY" 2>/dev/null | sort -u || true)"

TMP1="$(mktemp)"; TMP2="$(mktemp)"; trap 'rm -f "$TMP1" "$TMP2"' EXIT
printf '%s\n' "$RAW_DATES" > "$TMP1"
printf '%s\n' "$DAILY_DATES" > "$TMP2"
INTER="$(grep -Fxf "$TMP1" "$TMP2" | sort -u || true)"
[ -n "$INTER" ] || fail "(no overlapping date between raw and daily)"

check_date() {
d="$1"
dd="$(adb exec-out run-as "$APP" awk -F, -v d="$d" 'NR>1 && $1==d {print $3","$4; exit}' "$DAILY" 2>/dev/null | tr -d '\r' || true)"
[ -n "$dd" ] || { echo "MISS $d (no daily row)"; return; }
dd_del="$(printf '%s' "$dd" | cut -d, -f1)"
dd_op="$(printf '%s' "$dd" | cut -d, -f2)"

rd="$(adb exec-out run-as "$APP" awk -F, -v d="$d" '
NR==1{
for(i=1;i<=NF;i++){
n=$i; gsub(/\r/,"",n)
if(n=="ts"||n=="timestamp") tsi=i
if(n=="event") ei=i
if(n=="notif_id") idi=i
}
next
}
{
ts=$tsi; ev=toupper($ei); id=$idi
if(length(ts)<10||id=="") next
day=substr(ts,1,10)
if(day!=d) next
if(ev=="POSTED"||ev=="DELIVERED") pd[id]=1
else if(ev=="CLICKED"||ev=="CLICK"||ev=="OPENED") cd[id]=1
}
END{
pdn=0; for(k in pd) pdn++
cdn=0; for(k in cd) cdn++
printf "%d,%d\n", pdn, cdn
}' "$RAW" 2>/dev/null | tr -d '\r' || true)"
[ -n "$rd" ] || rd="0,0"
rd_del="$(printf '%s' "$rd" | cut -d, -f1)"
rd_op="$(printf '%s' "$rd" | cut -d, -f2)"

diff_abs(){ a=$1; b=$2; d=$((a-b)); [ $d -lt 0 ] && d=$((-d)); echo $d; }
ddel="$(diff_abs "$rd_del" "$dd_del")"
dopn="$(diff_abs "$rd_op" "$dd_op")"

if [ "$ddel" -le 1 ] && [ "$dopn" -le 1 ]; then
echo "OK  $d raw=${rd_del},${rd_op} daily=${dd_del},${dd_op}"
else
echo "BAD $d raw=${rd_del},${rd_op} daily=${dd_del},${dd_op}"
fi
}

RESULTS=""
while IFS= read -r d; do
[ -n "$d" ] || continue
RESULTS="${RESULTS}$(check_date "$d")\n"
done <<EOF_D
$INTER
EOF_D

printf "%b" "$RESULTS" | tee "$OUT"
printf "%b" "$RESULTS" | grep -q '^BAD ' && { echo "AT2 RESULT=FAIL" | tee -a "$OUT"; exit 1; }
echo "AT2 RESULT=PASS" | tee -a "$OUT"
exit 0
