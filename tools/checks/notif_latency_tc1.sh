#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
CSV="files/daily_notification_latency.csv"
OUT="evidence/v6.0/notification_latency/tc1.txt"
EXP_HDR="date,feature_schema_version,p50_ms,p90_ms,p99_ms,count"
mkdir -p "$(dirname "$OUT")"
fail(){ printf "TC-1 RESULT=FAIL %s\n" "$1" | tee "$OUT"; exit 1; }
adb get-state >/dev/null 2>&1 || fail "(no device)"
adb shell pm path "$PKG" >/dev/null 2>&1 || fail "(app not installed)"
CONTENT="$(adb shell run-as "$PKG" cat "$CSV" 2>/dev/null || true)"
[ -n "$CONTENT" ] || fail "($CSV missing or unreadable)"
HDR_RAW="$(printf '%s\n' "$CONTENT" | head -n1)"
HDR_CLEAN="$(printf '%s' "$HDR_RAW" | tr -d '\r' | awk 'BEGIN{ORS=""}{sub(/^\xef\xbb\xbf/,"");print}')"
# tolerate stray spaces around commas
HDR="$(printf '%s' "$HDR_CLEAN" | awk -F',' '{for(i=1;i<=NF;i++){gsub(/^ +| +$/,"",$i)}; for(i=1;i<=NF;i++){printf("%s%s",$i,(i<NF?",":""))}}')"
EXP_NORM="$(printf '%s' "$EXP_HDR" | awk -F',' '{for(i=1;i<=NF;i++){gsub(/^ +| +$/,"",$i)}; for(i=1;i<=NF;i++){printf("%s%s",$i,(i<NF?",":""))}}')"
BODY="$(printf '%s\n' "$CONTENT" | tail -n +2)"
[ "$HDR" = "$EXP_NORM" ] || { printf "HEADER_ACTUAL=%s\nHEADER_EXPECTED=%s\n" "$HDR" "$EXP_NORM" | tee "$OUT" >/dev/null; fail "(header drift)"; }
ymd_shift(){ b="$1"; s="$2"
if date -j -f "%Y-%m-%d" "$b" "+%F" >/dev/null 2>&1; then date -j -v"${s}"d -f "%Y-%m-%d" "$b" "+%F"; return; fi
if command -v gdate >/dev/null 2>&1; then gdate -d "$b $s day" +%F && return; fi
if date -d "$b $s day" +%F >/dev/null 2>&1; then date -d "$b $s day" +%F && return; fi
python3 - "$b" "$s" <<'PY' 2>/dev/null || echo "$b"
import sys,datetime
y,m,d=map(int,sys.argv[1].split("-")); off=int(sys.argv[2])
print((datetime.date(y,m,d)+datetime.timedelta(days=off)).strftime("%Y-%m-%d"))
PY
}
TODAY="$(adb shell date +%F 2>/dev/null | tr -d '\r')"; [ -n "$TODAY" ] || fail "(could not read device date)"
YEST="$(ymd_shift "$TODAY" -1)"; TOM="$(ymd_shift "$TODAY" +1)"
COUNTS="$(printf '%s\n' "$BODY" | awk -F',' 'NF>1{c[$1]++} END{for(d in c) printf("%s %d\n",d,c[d])}' | sort)"
CT="$(printf '%s\n' "$COUNTS" | awk -v d="$TODAY" '$1==d{print $2}')"
CY="$(printf '%s\n' "$COUNTS" | awk -v d="$YEST"  '$1==d{print $2}')"
CTo="$(printf '%s\n' "$COUNTS" | awk -v d="$TOM"   '$1==d{print $2}')"
DUPS="$(printf '%s\n' "$COUNTS" | awk '$2>1')"
PASS=1
[ -z "$DUPS" ] || PASS=0
[ -n "$CT" ] && [ "$CT" -ne 1 ] && PASS=0
[ -n "$CY" ] && [ "$CY" -ne 1 ] && PASS=0
REQ=0; { [ "${CT:-0}" = "1" ] || [ "${CY:-0}" = "1" ] || [ "${CTo:-0}" = "1" ]; } && REQ=1
[ "$REQ" -eq 1 ] || PASS=0
{
echo "HEADER_OK=1"
echo "TODAY=$TODAY"
echo "YESTERDAY=$YEST"
echo "TOMORROW=$TOM"
echo "COUNT_TODAY=${CT:-0}"
echo "COUNT_YESTERDAY=${CY:-0}"
echo "COUNT_TOMORROW=${CTo:-0}"
echo "DUPLICATES=$( [ -n "$DUPS" ] && printf 1 || printf 0 )"
} | tee "$OUT" >/dev/null
[ "$PASS" -eq 1 ] && echo "TC-1 RESULT=PASS" | tee -a "$OUT" && exit 0
echo "TC-1 RESULT=FAIL" | tee -a "$OUT"; exit 1
