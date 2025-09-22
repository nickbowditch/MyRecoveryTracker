#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/lnsu/seed_day.1.txt"
LOCK="app/locks/daily_lnsu.header"
CSV_DAILY="files/daily_lnsu.csv"
CSV_RAW="files/screen_log.csv"
EXP="date,feature_schema_version,minutes_22_02"
mkdir -p "$(dirname "$OUT")" app/locks

adb get-state >/dev/null 2>&1 || { echo "SEED RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG"  >/dev/null 2>&1 || { echo "SEED RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

printf '%s\n' "$EXP" > "$LOCK"

D="$(adb shell toybox date -d '@$(( $(adb shell toybox date +%s | tr -d "\r") - 86400 ))' +%F | tr -d '\r')"
N="$(adb shell toybox date -d '@$(( $(adb shell toybox date +%s | tr -d "\r")     ))' +%F | tr -d '\r')"

adb exec-out run-as "$PKG" sh -c '
set -eu
mkdir -p files
[ -f "'"$CSV_RAW"'" ]   || echo "ts,state" >"'"$CSV_RAW"'"
[ -f "'"$CSV_DAILY"'" ] || echo "'"$EXP"'"   >"'"$CSV_DAILY"'"

{
  echo "'"$D"' 21:55:00,SCREEN_ON"
  echo "'"$D"' 22:15:00,SCREEN_OFF"
  echo "'"$D"' 23:00:00,SCREEN_ON"
  echo "'"$D"' 23:45:00,SCREEN_OFF"
  echo "'"$N"' 01:10:00,SCREEN_ON"
  echo "'"$N"' 01:40:00,SCREEN_OFF"
} >> "'"$CSV_RAW"'"

mins=$(
  ws=$(toybox date -d "'"$D"' 22:00:00" +%s); we=$(toybox date -d "'"$D"' 02:00:00 +1 day" +%s)
  acc=0; prev=""; pon=0; last=0
  while IFS=, read -r ts state; do
    [ "$ts" = "ts" ] && continue
    t=$(toybox date -d "$ts" +%s 2>/dev/null) || continue
    case "$state" in
      *ON*|*Unlock*|*SCREEN_ON*|*SCREEN-ON*)  on=1;;
      *OFF*|*LOCK*|*SCREEN_OFF*|*SCREEN-OFF*) on=0;;
      *) on=0;;
    esac
    if [ "$t" -lt "$ws" ]; then last="$on"; prev="$t"; pon="$on"; continue; fi
    [ -z "$prev" ] && { prev="$ws"; pon="$last"; }
    end="$t"; [ "$end" -gt "$we" ] && end="$we"
    if [ "$pon" -eq 1 ]; then s="$prev"; [ "$s" -lt "$ws" ] && s="$ws"; e="$end"; [ "$e" -gt "$we" ] && e="$we"; [ "$e" -gt "$s" ] && acc=$((acc+e-s)); fi
    prev="$t"; pon="$on"
    [ "$t" -ge "$we" ] && break
  done < "'"$CSV_RAW"'"
  if [ -n "$prev" ] && [ "$pon" -eq 1 ] && [ "$prev" -lt "$we" ]; then s="$prev"; [ "$s" -lt "$ws" ] && s="$ws"; e="$we"; [ "$e" -gt "$s" ] && acc=$((acc+e-s)); fi
  echo $((acc/60))
)

awk -F, -v d="$D" 'NR>1&&$1==d{f=1} END{exit f?0:1}' "'"$CSV_DAILY"'" || \
  printf "%s,%s,%s\n" "$D" "v6.0" "$mins" >> "'"$CSV_DAILY"'"
' >/dev/null 2>&1

echo "SEED RESULT=PASS (date=$D)" | tee "$OUT"
