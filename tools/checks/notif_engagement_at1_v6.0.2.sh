#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
CSV_DAILY="files/daily_notification_engagement.csv"
CSV_RAW="files/notification_log.csv"
ACT="$PKG.ACTION_RUN_ENGAGEMENT_ROLLUP"
CMP="$PKG/.TriggerReceiver"
OUT="evidence/v6.0/notification_engagement/at1.2.txt"
EXP_HDR="date,feature_schema_version,delivered,opened,open_rate"
mkdir -p "$(dirname "$OUT")"

fail(){ echo "AT-1 RESULT=FAIL $1" | tee "$OUT"; exit 1; }

adb get-state >/dev/null 2>&1 || fail "(no device)"
adb shell pm path "$PKG" >/dev/null 2>&1 || fail "(app not installed)"

ymd_shift(){ b="$1"; s="$2"
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

TODAY="$(adb shell date +%F | tr -d '\r')"
[ -n "$TODAY" ] || fail "(device date unreadable)"
YEST="$(ymd_shift "$TODAY" -1)"

adb exec-out run-as "$PKG" sh -c '
set -eu
mkdir -p files
[ -f "'"$CSV_DAILY"'" ] || printf "%s\n" "'"$EXP_HDR"'" >"'"$CSV_DAILY"'"
[ -f "'"$CSV_RAW"'" ] || printf "ts,event,notif_id\n" >"'"$CSV_RAW"'"
' >/dev/null 2>&1 || fail "(prep failed)"

get_row_vals(){ awk -F, -v d="$1" 'NR>1&&$1==d{print $3","$4","$5;exit}'; }

before_today="$(adb exec-out run-as "$PKG" cat "$CSV_DAILY" 2>/dev/null | tr -d '\r' | get_row_vals "$TODAY")"
before_yest="$(adb exec-out run-as "$PKG"  cat "$CSV_DAILY" 2>/dev/null | tr -d '\r' | get_row_vals "$YEST")"

TS1="$TODAY 09:11:07"
TS2="$TODAY 09:12:09"

adb exec-out run-as "$PKG" sh -c '
set -eu
f="'"$CSV_RAW"'"
a="'"$TS1"'"; b="'"$TS2"'"
printf "%s,POSTED,at1-a\n%s,CLICKED,at1-a\n" "$a" "$b" >>"$f"
' >/dev/null 2>&1 || fail "(seed failed)"

adb shell am broadcast -a "$ACT" -n "$CMP" >/dev/null 2>&1 || true
sleep 2
adb shell am broadcast -a "$ACT" -n "$CMP" >/devnull 2>&1 || true

i=0
changed=0
while [ $i -lt 30 ]; do
  after_today="$(adb exec-out run-as "$PKG" cat "$CSV_DAILY" 2>/dev/null | tr -d '\r' | get_row_vals "$TODAY")"
  after_yest="$(adb exec-out run-as "$PKG"  cat "$CSV_DAILY" 2>/dev/null | tr -d '\r' | get_row_vals "$YEST")"
  if [ "$after_today" != "$before_today" ] || [ "$after_yest" != "$before_yest" ]; then changed=1; break; fi
  sleep 1; i=$((i+1))
done

adb exec-out run-as "$PKG" sh -c '
set -eu
in="'"$CSV_RAW"'"; tmp="${in}.tmp.$$"; a="'"$TS1"'"; b="'"$TS2"'"
awk -F, -v a="$a" -v b="$b" '"'"'NR==1{print;next}{ if(!($1==a && $2=="POSTED") && !($1==b && $2=="CLICKED")) print }'"'"' "$in" >"$tmp" && mv "$tmp" "$in"
' >/dev/null 2>&1 || true

[ "$changed" -eq 1 ] && { echo "AT-1 RESULT=PASS" | tee "$OUT"; exit 0; }
fail "(no change for today=$TODAY or yesterday=$YEST)"
