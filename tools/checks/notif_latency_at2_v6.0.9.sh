#!/bin/sh
set -eu
APP="com.nick.myrecoverytracker"
RAW="files/notification_latency_log.csv"
DAILY="files/daily_notification_latency.csv"
OUT="evidence/v6.0/notification_latency/at2.txt"
TOL=50

fail(){ echo "AT2 RESULT=FAIL $1" | tee "$OUT"; exit 1; }

adb get-state >/dev/null 2>&1 || fail "(no device)"
adb shell pm path "$APP" >/dev/null 2>&1 || fail "(app not installed)"

RAW_DATA="$(adb exec-out run-as "$APP" cat "$RAW" 2>/dev/null | tr -d '\r' || true)"
DAILY_DATA="$(adb exec-out run-as "$APP" cat "$DAILY" 2>/dev/null | tr -d '\r' || true)"
[ -n "$RAW_DATA" ] || fail "(missing raw)"
[ -n "$DAILY_DATA" ] || fail "(missing daily)"

RAW_HDR="$(printf '%s\n' "$RAW_DATA" | head -n1)"
DAILY_HDR="$(printf '%s\n' "$DAILY_DATA" | head -n1)"
case "$RAW_HDR" in
  "notif_id,posted_ts,opened_ts,latency_ms"|"notif_id,ts,opened_ts,latency_ms"|"notif_id,posted_ts,opened_ts,latency") : ;;
  *) fail "(bad raw header)";;
esac
[ "$DAILY_HDR" = "date,feature_schema_version,p50_ms,p90_ms,p99_ms,count" ] || fail "(bad daily header)"

TODAY="$(adb shell date +%F 2>/dev/null | tr -d '\r')"

RAW_DATES="$(printf '%s\n' "$RAW_DATA" | awk -F, '
NR==1{
  for(i=1;i<=NF;i++){
    h=$i; gsub(/\r/,"",h);
    if(h=="posted_ts"||h=="ts") pti=i
  }
  next
}
{
  pt=$pti
  if(pt!="" && length(pt)>=10){
    d=substr(pt,1,10)
    if(d ~ /^[0-9]{4}-[0-9]{2}-[0-9]{2}$/) print d
  }
}' | sort -u)"

DAILY_CLOSED="$(printf '%s\n' "$DAILY_DATA" | awk -F, -v t="$TODAY" 'NR>1 && $1 ~ /^[0-9]{4}-[0-9]{2}-[0-9]{2}$/ && $1<t {print $1}' | sort -u)"

TMP1="$(mktemp)"; TMP2="$(mktemp)"; trap 'rm -f "$TMP1" "$TMP2"' EXIT
printf '%s\n' "$RAW_DATES" > "$TMP1"
printf '%s\n' "$DAILY_CLOSED" > "$TMP2"
TARGET="$(grep -Fxf "$TMP1" "$TMP2" | sort | tail -n1 || true)"
[ -n "$TARGET" ] || fail "(no overlapping closed date between raw and daily)"

DD="$(printf '%s\n' "$DAILY_DATA" | awk -F, -v d="$TARGET" 'NR>1 && $1==d {print $3","$4","$5","$6; exit}')"
[ -n "$DD" ] || fail "(no daily row for $TARGET)"
DD_P50="$(printf '%s' "$DD" | cut -d, -f1)"
DD_P90="$(printf '%s' "$DD" | cut -d, -f2)"
DD_P99="$(printf '%s' "$DD" | cut -d, -f3)"
DD_CNT="$(printf '%s' "$DD" | cut -d, -f4)"

LAT_LIST="$(printf '%s\n' "$RAW_DATA" | awk -F, -v d="$TARGET" '
NR==1{
  for(i=1;i<=NF;i++){
    h=$i; gsub(/\r/,"",h);
    if(h=="posted_ts"||h=="ts") pti=i;
    if(h=="latency_ms"||h=="latency") li=i
  }
  next
}
{
  pt=$pti; lm=$li;
  if(pt!="" && length(pt)>=10 && substr(pt,1,10)==d){
    if(lm ~ /^[0-9]+$/) print lm
  }
}
')"
N="$(printf '%s\n' "$LAT_LIST" | grep -Ec '^[0-9]+$' || true)"; N="${N:-0}"

pct_from_sorted() {
  p="$1"
  awk -v p="$p" 'BEGIN{n=0} /^[0-9]+$/ {a[++n]=$1} END{
    if(n==0){print ""; exit}
    k = int((p*n + 99)/100); if(k<1)k=1; if(k>n)k=n
    print a[k]
  }'
}

if [ "$N" -eq 0 ]; then
  DD_CNT="${DD_CNT:-0}"
  [ "$DD_CNT" = "0" ] || { echo "BAD $TARGET cnt_raw=0 cnt_daily=$DD_CNT" | tee "$OUT"; echo "AT2 RESULT=FAIL" | tee -a "$OUT"; exit 1; }
  ok=1
  [ -z "${DD_P50:-}" ] || [ "${DD_P50:-}" = "0" ] || ok=0
  [ -z "${DD_P90:-}" ] || [ "${DD_P90:-}" = "0" ] || ok=0
  [ -z "${DD_P99:-}" ] || [ "${DD_P99:-}" = "0" ] || ok=0
  [ $ok -eq 1 ] || { echo "BAD $TARGET cnt=0 (percentiles not blank/0)" | tee "$OUT"; echo "AT2 RESULT=FAIL" | tee -a "$OUT"; exit 1; }
  {
    echo "OK  $TARGET cnt=0 (percentiles blank/0)"
    echo "AT2 RESULT=PASS"
  } | tee "$OUT"
  exit 0
fi

SORTED="$(printf '%s\n' "$LAT_LIST" | grep -E '^[0-9]+$' | sort -n)"
RP50="$(printf '%s\n' "$SORTED" | pct_from_sorted 50)"
RP90="$(printf '%s\n' "$SORTED" | pct_from_sorted 90)"
RP99="$(printf '%s\n' "$SORTED" | pct_from_sorted 99)"

printf '%s\n' "${DD_P50:-}" | grep -Eq '^[0-9]+$' || fail "(p50 non-numeric for $TARGET)"
printf '%s\n' "${DD_P90:-}" | grep -Eq '^[0-9]+$' || fail "(p90 non-numeric for $TARGET)"
printf '%s\n' "${DD_P99:-}" | grep -Eq '^[0-9]+$' || fail "(p99 non-numeric for $TARGET)"
printf '%s\n' "${DD_CNT:-}" | grep -Eq '^[0-9]+$' || fail "(count non-numeric for $TARGET)"

diff() { a="$1"; b="$2"; x=$((a-b)); [ $x -lt 0 ] && x=$(( -x )); echo "$x"; }

D50="$(diff "$RP50" "$DD_P50")"
D90="$(diff "$RP90" "$DD_P90")"
D99="$(diff "$RP99" "$DD_P99")"

if [ "$D50" -le "$TOL" ] && [ "$D90" -le "$TOL" ] && [ "$D99" -le "$TOL" ] && [ "$DD_CNT" -eq "$N" ]; then
  {
    echo "OK  $TARGET p50=$DD_P50~$RP50 p90=$DD_P90~$RP90 p99=$DD_P99~$RP99 n=$N"
    echo "AT2 RESULT=PASS"
  } | tee "$OUT"
else
  {
    echo "BAD $TARGET p50=$DD_P50~$RP50(d=$D50) p90=$DD_P90~$RP90(d=$D90) p99=$DD_P99~$RP99(d=$D99) daily_n=$DD_CNT raw_n=$N"
    echo "AT2 RESULT=FAIL"
  } | tee "$OUT"
  exit 1
fi
