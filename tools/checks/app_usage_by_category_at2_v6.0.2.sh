#!/bin/sh
set -eu
APP="com.nick.myrecoverytracker"
CSV="files/app_category_daily.csv"
BYAPP="files/daily_app_usage_minutes_by_app.csv"
RCV="$APP/.TriggerReceiver"
ACT="com.nick.myrecoverytracker.ACTION_RUN_APP_CATEGORY_DAILY"
OUT="evidence/v6.0/app_usage_by_category/at2.txt"
mkdir -p "$(dirname "$OUT")"
fail(){ echo "AT2 RESULT=FAIL $1" | tee "$OUT"; exit 1; }

adb get-state >/dev/null 2>&1 || fail "(no device)"
adb shell pm path "$APP" >/dev/null 2>&1 || fail "(app not installed)"

CH="$(adb exec-out run-as "$APP" sed -n '1p' "$CSV" 2>/dev/null | tr -d '\r' || true)"
[ "$CH" = "date,category,minutes" ] || fail "(bad category header)"

T="$(adb shell toybox date +%F | tr -d '\r')"

adb exec-out run-as "$APP" sh -c '
set -eu
mkdir -p files
catf="'"$CSV"'"
appf="'"$BYAPP"'"
toybox rm -f "$catf" "$appf"
echo "date,category,minutes" >"$catf"
echo "date,category,minutes" >"$appf"
echo "'"$T"',social,30.0" >>"$appf"
echo "'"$T"',social,15.0" >>"$appf"
echo "'"$T"',productivity,45.0" >>"$appf"
' >/dev/null 2>&1 || true

adb shell cmd activity broadcast -n "$RCV" -a "$ACT" --receiver-foreground --user 0 >/dev/null 2>&1 || fail "(broadcast failed)"
sleep 2

REBUILT="$(adb exec-out run-as "$APP" awk -F, -v d="$T" 'NR>1&&$1==d{sum[$2]+=$3} END{for(k in sum) printf "%s,%.1f\n",k,sum[k]}' "$BYAPP" 2>/dev/null | tr -d '\r' || true)"
ACTUAL="$(adb exec-out run-as "$APP" awk -F, -v d="$T" 'NR>1&&$1==d{printf "%s,%.1f\n",$2,$3}' "$CSV" 2>/dev/null | tr -d '\r' || true)"

DIFF_OK=1
if [ -n "$REBUILT" ] && [ -n "$ACTUAL" ]; then
  while IFS=, read -r cat min; do
    [ -z "$cat" ] && continue
    exp="$(printf '%s\n' "$ACTUAL" | awk -F, -v c="$cat" '$1==c{print $2;exit}')"
    [ -z "$exp" ] && { DIFF_OK=0; break; }
    diff="$(awk -v a="$min" -v b="$exp" 'BEGIN{d=(a-b);if(d<0)d=-d;print d}')"
    awk -v d="$diff" 'BEGIN{exit (d>1)?1:0}' || { DIFF_OK=0; break; }
  done <<EOF2
$REBUILT
EOF2
else
  DIFF_OK=0
fi

{
echo "--- REBUILT ---"
printf '%s\n' "$REBUILT"
echo "--- ACTUAL ---"
printf '%s\n' "$ACTUAL"
if [ "$DIFF_OK" -eq 1 ]; then
  echo "AT2 RESULT=PASS"
  exit 0
else
  echo "AT2 RESULT=FAIL (category diff > tolerance)"
  exit 1
fi
} | tee "$OUT"
