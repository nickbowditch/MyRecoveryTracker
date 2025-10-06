#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
CSV="files/app_category_daily.csv"
LOCK="app/locks/daily_app_usage_by_category.header"
RCV="$PKG/.TriggerReceiver"
ACT="$PKG.ACTION_RUN_APP_CATEGORY_DAILY"
OUT="evidence/v6.0/app_usage_by_category/gv7.1.txt"
mkdir -p "$(dirname "$OUT")" app/locks

adb get-state >/dev/null 2>&1 || { echo "GV7 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "GV7 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

T="$(adb shell date +%F | tr -d '\r')"

adb shell run-as "$PKG" sh <<IN
set -eu
csv="$CSV"
hdr="date,category,minutes"
mkdir -p files app/locks
echo "\$hdr" > "\$csv"
echo "\$hdr" > "$LOCK"
IN

adb shell cmd activity broadcast -n "$RCV" -a "$ACT" --receiver-foreground --user 0 >/dev/null 2>&1 || true
sleep 2

hdr="$(adb exec-out run-as "$PKG" head -n1 "$CSV" | tr -d '\r')"
row="$(adb exec-out run-as "$PKG" awk -F, -v d="$T" 'NR>1&&$1==d{print;exit}' "$CSV" | tr -d '\r')"

[ "$hdr" = "date,category,minutes" ] || { echo "GV7 RESULT=FAIL (bad header: $hdr)" | tee "$OUT"; exit 1; }
[ -n "$row" ] || { echo "GV7 RESULT=FAIL (missing row for $T)" | tee "$OUT"; exit 1; }

mins="$(echo "$row" | awk -F, '{print $3}')"
awk -v m="$mins" 'BEGIN{exit (m>=0 && m<=1440)?0:1}' || { echo "GV7 RESULT=FAIL (invalid minutes=$mins)" | tee "$OUT"; exit 1; }

echo "GV7 RESULT=PASS" | tee "$OUT"
exit 0
