#!/bin/sh
set -eu
APP="com.nick.myrecoverytracker"
RAW="files/notification_latency_log.csv"
DAILY="files/daily_notification_latency.csv"
OUT_DIR="evidence/v6.0/notification_latency"
OUT="$OUT_DIR/at2.txt"
TOL=50
mkdir -p "$OUT_DIR"

fail(){ echo "AT2 RESULT=FAIL $1" | tee "$OUT"; exit 1; }

adb get-state >/dev/null 2>&1 || fail "(no device)"
adb shell pm path "$APP" >/dev/null 2>&1 || fail "(app not installed)"

RAW_HDR="$(adb exec-out run-as "$APP" sed -n '1p' "$RAW" 2>/dev/null | tr -d '\r' || true)"
DAILY_HDR="$(adb exec-out run-as "$APP" sed -n '1p' "$DAILY" 2>/dev/null | tr -d '\r' || true)"
[ -n "$RAW_HDR" ] || fail "(missing raw)"
[ -n "$DAILY_HDR" ] || fail "(missing daily)"
[ "$RAW_HDR" = "notif_id,posted_ts,opened_ts,latency_ms" ] || fail "(bad raw header)"
[ "$DAILY_HDR" = "date,feature_schema_version,p50_ms,p90_ms,p99_ms,count" ] || fail "(bad daily header)"

TODAY="$(adb shell date +%F 2>/dev/null | tr -d '\r')"

RAW_DATES="$(adb exec-out run-as "$APP" awk -F, 'NR>1 && length($2)>=10 {print substr($2,1,10)}' "$RAW" 2>/dev/null | sort -u)"
DAILY_CLOSED="$(adb exec-out run-as "$APP" awk -F, -v t="$TODAY" 'NR>1 && $1~ /^[0-9]{4}-[0-9]{2}-[0-9]{2}$/ && $1<t {print $1}' "$DAILY" 2>/dev/null | sort -u)"

TMP1="$(mktemp)"; TMP2="$(mktemp)"; trap 'rm -f "$TMP1" "$TMP2"' EXIT
printf '%s\n' "$RAW_DATES" > "$TMP1"
printf '%s\n' "$DAILY_CLOSED" > "$TMP2"
TARGET="$(grep -Fxf "$TMP1" "$TMP2" | sort | tail -n1 || true)"
[ -n "$TARGET" ] || fail "(no overlapping closed date between raw and daily)"

DD_LINE="$(adb exec-out run-as "$APP" awk -F, -v d="$TARGET" 'NR>1 && $1==d {printf "%s %s %s %s\n",$3,$4,$5,$6; exit}' "$DAILY" 2>/dev/null)"
[ -n "$DD_LINE" ] || fail "(no daily row for $TARGET)"
set -- $DD_LINE; DD50="$1"; DD90="$2"; DD99="$3"; DN="$4"

LAT_LIST="$(adb exec-out run-as "$APP" awk -F, -v d="$TARGET" 'NR>1 && substr($2,1,10)==d && $4 ~ /^[0-9]+$/ {print $4}' "$RAW" 2>/dev/null | sort -n || true)"
RN="$(printf '%s\n' "$LAT_LIST" | grep -Ec '^[0-9]+$' || true)"; RN="${RN:-0}"

pct_from_sorted_interp() {
p="$1"
awk -v p="$p" '
BEGIN{n=0}
/^[0-9]+$/ {a[++n]=$1}
END{
if(n==0){print 0; exit}
r=p/100*(n-1)
lo=int(r)+1
hi=(lo<n)?lo+1:lo
frac=r-int(r)
val=a[lo]*(1-frac)+a[hi]*frac
printf "%d\n", val+0.5
}'
}

if [ "$RN" -eq 0 ]; then
DN="${DN:-0}"
[ "$DN" = "0" ] || { echo "BAD $TARGET cnt_raw=0 cnt_daily=$DN" | tee "$OUT"; echo "AT2 RESULT=FAIL" | tee -a "$OUT"; exit 1; }
ok=1
[ -z "${DD50:-}" ] || [ "${DD50:-}" = "0" ] || ok=0
[ -z "${DD90:-}" ] || [ "${DD90:-}" = "0" ] || ok=0
[ -z "${DD99:-}" ] || [ "${DD99:-}" = "0" ] || ok=0
[ $ok -eq 1 ] || { echo "BAD $TARGET cnt=0 (percentiles not blank/0)" | tee "$OUT"; echo "AT2 RESULT=FAIL" | tee -a "$OUT"; exit 1; }
{
echo "OK  $TARGET cnt=0 (percentiles blank/0)"
echo "AT2 RESULT=PASS"
} | tee "$OUT"
exit 0
fi

RP50="$(printf '%s\n' "$LAT_LIST" | pct_from_sorted_interp 50)"
RP90="$(printf '%s\n' "$LAT_LIST" | pct_from_sorted_interp 90)"
RP99="$(printf '%s\n' "$LAT_LIST" | pct_from_sorted_interp 99)"

diff() { a="$1"; b="$2"; x=$((a-b)); [ $x -lt 0 ] && x=$(( -x )); echo "$x"; }
D50="$(diff "${RP50:-0}" "${DD50:-0}")"
D90="$(diff "${RP90:-0}" "${DD90:-0}")"
D99="$(diff "${RP99:-0}" "${DD99:-0}")"

if [ "$D50" -le "$TOL" ] && [ "$D90" -le "$TOL" ] && [ "$D99" -le "$TOL" ] && [ "$RN" -eq "${DN:-0}" ]; then
{
echo "OK  $TARGET p50=$DD50~$RP50 p90=$DD90~$RP90 p99=$DD99~$RP99 n=$RN"
echo "AT2 RESULT=PASS"
} | tee "$OUT"
else
{
echo "BAD $TARGET p50=$DD50~$RP50(d=$D50) p90=$DD90~$RP90(d=$D90) p99=$DD99~$RP99(d=$D99) daily_n=${DN:-0} raw_n=$RN"
echo "AT2 RESULT=FAIL"
} | tee "$OUT"
exit 1
fi
