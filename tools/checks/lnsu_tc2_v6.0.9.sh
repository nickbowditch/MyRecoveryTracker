#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/lnsu/tc2.9.txt"
CSV_DAILY="files/daily_lnslu.csv"
CSV_RAW="files/screen_log.csv"
LOCK="app/locks/daily_lnslu.header"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "TC-2 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG"  >/dev/null 2>&1 || { echo "TC-2 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

EXP="$(tr -d '\r' < "$LOCK" 2>/dev/null || true)"
[ -n "$EXP" ] || { echo "TC-2 RESULT=FAIL (missing lock)" | tee "$OUT"; exit 4; }
HDR="$(adb exec-out run-as "$PKG" head -n1 "$CSV_DAILY" 2>/dev/null | tr -d '\r' || true)"
[ "$HDR" = "$EXP" ] || { echo "TC-2 RESULT=FAIL (bad header)" | tee "$OUT"; exit 5; }

D="$(adb exec-out run-as "$PKG" awk -F, 'NR>1{d=$1} END{print d}' "$CSV_DAILY" 2>/dev/null | tr -d '\r' || true)"
[ -n "$D" ] || { echo "TC-2 RESULT=FAIL (no rows)" | tee "$OUT"; exit 6; }

DAILY_VAL="$(adb exec-out run-as "$PKG" awk -F, -v d="$D" 'NR>1&&$1==d{print $2;exit}' "$CSV_DAILY" 2>/dev/null | tr -d '\r' || echo 0)"
[ -n "$DAILY_VAL" ] || DAILY_VAL=0

RECOMP="$(adb exec-out run-as "$PKG" sh -c '
set -eu
raw="'"$CSV_RAW"'"; d="'"$D"'"
[ -f "$raw" ] || { echo 0; exit 0; }
n=$(toybox date -d "$d +1 day" +%F 2>/dev/null || echo "$d")
ws=$(toybox date -d "$d 22:00:00" +%s 2>/dev/null || echo 0)
we=$(toybox date -d "$d 02:00:00 +1 day" +%s 2>/dev/null || echo 0)
acc=0; prev_ts=""; prev_on=0; last_on=0
awk -F, -v d="$d" -v n="$n" "NR==1{next}{dd=substr(\$1,1,10); if(dd<d) next; if(dd>n) exit; print}" "$raw" \
| while IFS=, read -r ts ev; do
  t=$(toybox date -d "$ts" +%s 2>/dev/null) || continue
  case "$ev" in
    ON|Unlock|SCREEN_ON|SCREEN-ON)  cur_on=1 ;;
    OFF|LOCK|SCREEN_OFF|SCREEN-OFF) cur_on=0 ;;
    *) cur_on=0 ;;
  esac
  if [ "$t" -lt "$ws" ]; then last_on="$cur_on"; prev_ts="$t"; prev_on="$cur_on"; continue; fi
  [ -z "$prev_ts" ] && { prev_ts="$ws"; prev_on="$last_on"; }
  seg_end="$t"; [ "$seg_end" -gt "$we" ] && seg_end="$we"
  if [ "$prev_on" -eq 1 ]; then
    s="$prev_ts"; [ "$s" -lt "$ws" ] && s="$ws"
    e="$seg_end"; [ "$e" -gt "$we" ] && e="$we"
    [ "$e" -gt "$s" ] && acc=$(( acc + e - s ))
  fi
  prev_ts="$t"; prev_on="$cur_on"
  [ "$t" -ge "$we" ] && break
done
if [ -n "$prev_ts" ] && [ "$prev_on" -eq 1 ] && [ "$prev_ts" -lt "$we" ]; then
  s="$prev_ts"; [ "$s" -lt "$ws" ] && s="$ws"; e="$we"; [ "$e" -gt "$s" ] && acc=$(( acc + e - s ))
fi
echo $(( acc / 60 ))
')"

printf 'TC-2 D=%s recomputed=%s daily=%s\n' "$D" "$RECOMP" "$DAILY_VAL" | tee "$OUT" >/dev/null
if [ "${RECOMP:-0}" = "${DAILY_VAL:-0}" ]; then
  echo "TC-2 RESULT=PASS" | tee -a "$OUT"; exit 0
else
  echo "TC-2 RESULT=FAIL" | tee -a "$OUT"; exit 1
fi
