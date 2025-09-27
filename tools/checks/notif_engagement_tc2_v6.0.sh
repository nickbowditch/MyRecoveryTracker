#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
IN="files/notification_log.csv"
OUTCSV="files/daily_notification_engagement.csv"
EVID="evidence/v6.0/notification_engagement/tc2.txt"
EXP_HDR="date,feature_schema_version,delivered,opened,open_rate"
RCV="$PKG/.TriggerReceiver"
A1="$PKG.ACTION_RUN_NOTIFICATION_ENGAGEMENT_ROLLUP"
A2="$PKG.ACTION_RUN_ENGAGEMENT_ROLLUP"

mkdir -p "$(dirname "$EVID")"
fail(){ printf "TC-2 RESULT=FAIL %s\n" "$1" | tee "$EVID"; exit 1; }

adb get-state >/dev/null 2>&1 || fail "(no device)"
adb shell pm path "$PKG" >/dev/null 2>&1 || fail "(app not installed)"

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

TS_Y_P="$YEST 23:59:00"
TS_Y_C="$YEST 23:59:10"
TS_T_P="$TODAY 00:01:00"
TS_T_C="$TODAY 00:01:15"

adb shell run-as "$PKG" sh -c "rm -f '$OUTCSV' '$IN' && mkdir -p files && printf '%s\n' \
'ts,event,notif_id' \
'$TS_Y_P,POSTED,tc2-a' \
'$TS_Y_C,CLICKED,tc2-a' \
'$TS_T_P,POSTED,tc2-b' \
'$TS_T_C,CLICKED,tc2-b' > '$IN'" || fail "(seed failed)"

adb shell cmd activity broadcast -n "$RCV" -a "$A1" --receiver-foreground --user 0 >/dev/null 2>&1 || true
adb shell cmd activity broadcast -n "$RCV" -a "$A2" --receiver-foreground --user 0 >/dev/null 2>&1 || true

i=0
while [ $i -lt 30 ]; do
  H="$(adb shell run-as "$PKG" head -n1 "$OUTCSV" 2>/dev/null | tr -d '\r' || true)"
  if [ "$H" = "$EXP_HDR" ]; then
    RY="$(adb shell run-as "$PKG" awk -F',' -v d="$YEST" '$1==d{print $0}' "$OUTCSV" 2>/dev/null || true)"
    RT="$(adb shell run-as "$PKG" awk -F',' -v d="$TODAY" '$1==d{print $0}' "$OUTCSV" 2>/dev/null || true)"
    [ -n "$RY" ] && [ -n "$RT" ] && break
  fi
  i=$((i+1)); sleep 1
done

HDR_OK=0
Y_OK=0
T_OK=0
HDR="$(adb shell run-as "$PKG" head -n1 "$OUTCSV" 2>/dev/null | tr -d '\r' || true)"
[ "$HDR" = "$EXP_HDR" ] && HDR_OK=1

RY="$(adb shell run-as "$PKG" awk -F',' -v d="$YEST" '$1==d{print $0}' "$OUTCSV" 2>/dev/null || true)"
RT="$(adb shell run-as "$PKG" awk -F',' -v d="$TODAY" '$1==d{print $0}' "$OUTCSV" 2>/dev/null || true)"

if [ -n "$RY" ]; then
  DY="$(printf '%s\n' "$RY" | awk -F',' '{print $3+0}')"
  OY="$(printf '%s\n' "$RY" | awk -F',' '{print $4+0}')"
  [ "$DY" -ge 1 ] && [ "$OY" -ge 1 ] && Y_OK=1
else DY=0; OY=0; fi

if [ -n "$RT" ]; then
  DT="$(printf '%s\n' "$RT" | awk -F',' '{print $3+0}')"
  OT="$(printf '%s\n' "$RT" | awk -F',' '{print $4+0}')"
  [ "$DT" -ge 1 ] && [ "$OT" -ge 1 ] && T_OK=1
else DT=0; OT=0; fi

{
  echo "HEADER_OK=$HDR_OK"
  echo "YESTERDAY=$YEST DELIVERED=$DY OPENED=$OY"
  echo "TODAY=$TODAY DELIVERED=$DT OPENED=$OT"
} | tee "$EVID" >/dev/null

if [ "$HDR_OK" -eq 1 ] && [ "$Y_OK" -eq 1 ] && [ "$T_OK" -eq 1 ]; then
  echo "TC-2 RESULT=PASS" | tee -a "$EVID"
  exit 0
else
  echo "TC-2 RESULT=FAIL" | tee -a "$EVID"
  exit 1
fi
