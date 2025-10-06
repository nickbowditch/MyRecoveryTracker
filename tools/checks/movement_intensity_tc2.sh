#!/bin/bash
set -euo pipefail
PKG="com.nick.myrecoverytracker"
CSV_DAY="files/daily_movement_intensity.csv"
CSV_LOG="files/movement_log.csv"
OUT="evidence/v6.0/movement_intensity/tc2.txt"

mkdir -p "$(dirname "$OUT")"
exec > >(tee "$OUT") 2>&1
fail(){ echo "TC2 RESULT=FAIL ($1)"; exit 1; }

adb get-state >/dev/null 2>&1 || { echo "TC2 RESULT=FAIL (no device)"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "TC2 RESULT=FAIL (app not installed)"; exit 3; }

HDR_DAY="$(adb exec-out run-as "$PKG" sed -n '1p' "$CSV_DAY" 2>/dev/null | tr -d '\r' || true)"
[ "$HDR_DAY" = "date,intensity" ] || fail "daily CSV header mismatch or missing"

TMP_LOG="$(mktemp)"
adb exec-out run-as "$PKG" cat "$CSV_LOG" 2>/dev/null | tr -d '\r' > "$TMP_LOG" || true
[ -s "$TMP_LOG" ] || fail "movement_log missing or empty"

TODAY="$(adb shell 'toybox date +%F 2>/dev/null || date +%F' | tr -d $'\r')"
[ -n "$TODAY" ] || fail "date read error"
YDAY="$( (date -j -f '%Y-%m-%d' "$TODAY" -v-1d +%F 2>/dev/null) || (date -d "$TODAY -1 day" +%F 2>/dev/null) || true )"
[ -n "$YDAY" ] || fail "cannot compute YDAY from TODAY=$TODAY"

BAD_TIME_FMT_COUNT="$(awk -F, '
  {
    split($1, a, /[ T]/)
    d=a[1]; t=a[2]
    if (d !~ /^[0-9]{4}-[0-9]{2}-[0-9]{2}$/ || t !~ /^[0-9]{2}:[0-9]{2}:[0-9]{2}$/) next
    split(t, b, /:/)
    hh=b[1]+0; mm=b[2]+0; ss=b[3]+0
    if (b[1] !~ /^[0-9][0-9]$/ || b[2] !~ /^[0-9][0-9]$/ || b[3] !~ /^[0-9][0-9]$/) {bad++ ; next}
    if (hh<0 || hh>23 || mm<0 || mm>59 || ss<0 || ss>59) bad++
  }
  END{print bad+0}
' "$TMP_LOG")"

if [ "$BAD_TIME_FMT_COUNT" -gt 0 ]; then
  echo "--- DEBUG: bad time format samples ---"
  awk -F, '
    {
      split($1, a, /[ T]/); d=a[1]; t=a[2]
      if (d !~ /^[0-9]{4}-[0-9]{2}-[0-9]{2}$/ || t !~ /^[0-9]{2}:[0-9]{2}:[0-9]{2}$/) print $0
    }
  ' "$TMP_LOG" | head -n 10
  rm -f "$TMP_LOG"
  fail "invalid timestamp format in movement_log"
fi

BAD_AFTER_MIDNIGHT_COUNT="$(awk -F, -v y="$YDAY" '
  {
    split($1, a, /[ T]/); d=a[1]; t=a[2]
    if (d ~ /^[0-9]{4}-[0-9]{2}-[0-9]{2}$/ && t ~ /^[0-9]{2}:[0-9]{2}:[0-9]{2}$/)
      if (d==y && t>="00:00:00" && t<"00:10:00") bad++
  }
  END{print bad+0}
' "$TMP_LOG")"

BAD_BEFORE_MIDNIGHT_COUNT="$(awk -F, -v tdy="$TODAY" '
  {
    split($1, a, /[ T]/); d=a[1]; t=a[2]
    if (d ~ /^[0-9]{4}-[0-9]{2}-[0-9]{2}$/ && t ~ /^[0-9]{2}:[0-9]{2}:[0-9]{2}$/)
      if (d==tdy && t>="23:50:00" && t<="23:59:59") bad++
  }
  END{print bad+0}
' "$TMP_LOG")"

echo "today=$TODAY"
echo "yesterday=$YDAY"
echo "--- DEBUG: YDAY 23:50-23:59 ---"
awk -F, -v y="$YDAY" '{split($1,a,/[ T]/);d=a[1];t=a[2]; if(d==y && t>="23:50:00" && t<="23:59:59") print $0}' "$TMP_LOG" | head -n 10
echo "--- DEBUG: TODAY 00:00-00:10 ---"
awk -F, -v tdy="$TODAY" '{split($1,a,/[ T]/);d=a[1];t=a[2]; if(d==tdy && t>="00:00:00" && t<"00:10:00") print $0}' "$TMP_LOG" | head -n 10
echo "--- DEBUG: BAD after-midnight labeled YDAY (count=$BAD_AFTER_MIDNIGHT_COUNT) ---"
awk -F, -v y="$YDAY" '{split($1,a,/[ T]/);d=a[1];t=a[2]; if(d==y && t>="00:00:00" && t<"00:10:00") print $0}' "$TMP_LOG" | head -n 10
echo "--- DEBUG: BAD before-midnight labeled TODAY (count=$BAD_BEFORE_MIDNIGHT_COUNT) ---"
awk -F, -v tdy="$TODAY" '{split($1,a,/[ T]/);d=a[1];t=a[2]; if(d==tdy && t>="23:50:00" && t<="23:59:59") print $0}' "$TMP_LOG" | head -n 10

rm -f "$TMP_LOG"

if [ "$BAD_AFTER_MIDNIGHT_COUNT" -gt 0 ] || [ "$BAD_BEFORE_MIDNIGHT_COUNT" -gt 0 ]; then
  fail "midnight split violated (entries on wrong date near boundary)"
fi

echo "TC2 RESULT=PASS"
exit 0
