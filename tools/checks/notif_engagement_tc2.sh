#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
RAW="files/notification_log.csv"
DAILY="files/daily_notification_engagement.csv"
OUT="evidence/v6.0/notification_engagement/tc2.txt"
RCV="$PKG/.TriggerReceiver"
A1="$PKG.ACTION_RUN_NOTIFICATION_ENGAGEMENT_ROLLUP"
A2="$PKG.ACTION_RUN_ENGAGEMENT_ROLLUP"
EXP_HDR="date,feature_schema_version,delivered,opened,open_rate"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "TC-2 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "TC-2 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

TODAY="$(adb shell date +%F | tr -d '\r')"
YEST="$(date -v-1d +%F 2>/dev/null || gdate -d "$TODAY -1 day" +%F 2>/dev/null || date -d "$TODAY -1 day" +%F)"

TS_Y_P="$YEST 23:59:00"
TS_Y_C="$YEST 23:59:10"
TS_T_P="$TODAY 00:01:00"
TS_T_C="$TODAY 00:01:15"

adb exec-out run-as "$PKG" sh -c '
mkdir -p files app/locks
[ -f "'"$RAW"'" ]   || printf "ts,event,notif_id\n" >"'"$RAW"'"
[ -f "'"$DAILY"'" ] || printf "'"$EXP_HDR"'\n" >"'"$DAILY"'"
' >/dev/null

before_y="$(adb exec-out run-as "$PKG" awk -F, -v d="$YEST" 'NR>1&&$1==d{print $0}' "$DAILY" | tr -d '\r')"
before_t="$(adb exec-out run-as "$PKG" awk -F, -v d="$TODAY" 'NR>1&&$1==d{print $0}' "$DAILY" | tr -d '\r')"

adb exec-out run-as "$PKG" sh -c '
f="'"$RAW"'"
printf "%s,POSTED,tc2-a\n%s,CLICKED,tc2-a\n%s,POSTED,tc2-b\n%s,CLICKED,tc2-b\n" \
"'"$TS_Y_P"'" "'"$TS_Y_C"'" "'"$TS_T_P"'" "'"$TS_T_C"'" >>"$f"
' >/dev/null

deadline=$(( $(date +%s) + 40 ))
ok=1
while :; do
adb shell am broadcast -a "$A1" -n "$RCV" >/dev/null 2>&1 || true
adb shell am broadcast -a "$A2" -n "$RCV" >/dev/null 2>&1 || true
sleep 2
row_y="$(adb exec-out run-as "$PKG" awk -F, -v d="$YEST" 'NR>1&&$1==d{print $0}' "$DAILY" | tr -d '\r')"
row_t="$(adb exec-out run-as "$PKG" awk -F, -v d="$TODAY" 'NR>1&&$1==d{print $0}' "$DAILY" | tr -d '\r')"
if [ -n "$row_y" ] && [ -n "$row_t" ]; then ok=0; break; fi
[ "$(date +%s)" -ge "$deadline" ] && break
done

adb exec-out run-as "$PKG" sh -c '
in="'"$RAW"'"; tmp="${in}.tmp.$$"
awk -F, -v a="'"$TS_Y_P"'" -v b="'"$TS_Y_C"'" -v c="'"$TS_T_P"'" -v d="'"$TS_T_C"'" \
"NR==1{print;next}{if(\$1!=a && \$1!=b && \$1!=c && \$1!=d) print}" "$in" >"$tmp" && mv "$tmp" "$in"
' >/dev/null || true

if [ $ok -eq 0 ]; then
echo "TC-2 RESULT=PASS" | tee "$OUT"
exit 0
else
echo "TC-2 RESULT=FAIL" | tee "$OUT"
exit 1
fi
