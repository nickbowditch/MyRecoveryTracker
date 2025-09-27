#!/bin/sh
set -eu

PKG="com.nick.myrecoverytracker"
CSV="files/daily_notification_engagement.csv"
OUT="evidence/v6.0/notification_engagement/tc1.txt"
EXP_HDR="date,feature_schema_version,delivered,opened,open_rate"

mkdir -p "$(dirname "$OUT")"

fail() { printf "%s\n" "TC-1 RESULT=FAIL $1" | tee "$OUT"; exit 1; }

adb get-state >/dev/null 2>&1 || fail "(no device)"
adb shell pm path "$PKG" >/dev/null 2>&1 || fail "(app not installed)"

CONTENT="$(adb shell run-as "$PKG" cat "$CSV" 2>/dev/null || true)"
[ -n "$CONTENT" ] || fail "($CSV missing or unreadable)"

HDR="$(printf '%s\n' "$CONTENT" | head -n1 | tr -d '\r')"
BODY="$(printf '%s\n' "$CONTENT" | tail -n +2)"
[ "$HDR" = "$EXP_HDR" ] || fail "(header drift)"

ymd_shift() {
  b="$1"; s="$2"
  if date -j -f "%Y-%m-%d" "$b" "+%F" >/dev/null 2>&1; then date -j -v"${s}"d -f "%Y-%m-%d" "$b" "+%F"; return; fi
  if command -v gdate >/dev/null 2>&1; then gdate -d "$b $s day" +%F && return; fi
  if date -d "$b $s day" +%F >/dev/null 2>&1; then date -d "$b $s day" +%F && return; fi
  if command -v python3 >/dev/null 2>&1; then python3 - "$b" "$s" <<'PY'
import sys,datetime
y,m,d=map(int,sys.argv[1].split("-")); off=int(sys.argv[2])
print((datetime.date(y,m,d)+datetime.timedelta(days=off)).strftime("%Y-%m-%d"))
PY
  else echo "$b"; fi
}

TODAY="$(adb shell date +%F 2>/dev/null | tr -d '\r')"
[ -n "$TODAY" ] || fail "(could not read device date)"
YEST="$(ymd_shift "$TODAY" -1)"
TOM="$(ymd_shift "$TODAY" +1)"

COUNTS="$(printf '%s\n' "$BODY" | awk -F',' 'NF>1{c[$1]++} END{for(d in c) printf("%s %d\n",d,c[d])}' | sort)"
CT="$(printf '%s\n' "$COUNTS" | awk -v d="$TODAY" '$1==d{print $2}')"
CY="$(printf '%s\n' "$COUNTS" | awk -v d="$YEST"  '$1==d{print $2}')"
CTo="$(printf '%s\n' "$COUNTS" | awk -v d="$TOM"   '$1==d{print $2}')"
DUPS="$(printf '%s\n' "$COUNTS" | awk '$2>1')"

PASS=1
[ -z "$DUPS" ] || PASS=0
[ -n "$CT" ]  && [ "$CT"  -ne 1 ] && PASS=0
[ -n "$CY" ]  && [ "$CY"  -ne 1 ] && PASS=0
REQ=0
{ [ "$CT" = "1" ] || [ "$CY" = "1" ] || [ "$CTo" = "1" ]; } && REQ=1
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

if [ "$PASS" -eq 1 ]; then
  echo "TC-1 RESULT=PASS" | tee -a "$OUT"
  exit 0
else
  echo "TC-1 RESULT=FAIL" | tee -a "$OUT"
  exit 1
fi
