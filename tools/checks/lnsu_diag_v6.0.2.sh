#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/lnsu/diag.2.txt"
CSV_DAILY="files/daily_lnsu.csv"
CSV_RAW="files/screen_log.csv"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "LNSU-DIAG RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG"  >/dev/null 2>&1 || { echo "LNSU-DIAG RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

TZSTR="$(adb shell toybox date +%Z_%z 2>/dev/null | tr -d '\r')"

HDR="$(adb exec-out run-as "$PKG" head -n1 "$CSV_DAILY" 2>/dev/null | tr -d '\r' || true)"
[ -n "$HDR" ] || { echo "LNSU-DIAG RESULT=FAIL (missing daily_lnsu.csv)" | tee "$OUT"; exit 4; }

D="$(adb exec-out run-as "$PKG" awk -F, 'NR>1{d=$1} END{print d}' "$CSV_DAILY" 2>/dev/null | tr -d '\r')"
if [ -z "$D" ]; then
  EPOCH="$(adb shell toybox date +%s 2>/dev/null | tr -d '\r')"
  YEPO=$(( ${EPOCH:-0} - 86400 ))
  D="$(adb shell toybox date -d "@$YEPO" +%F 2>/dev/null | tr -d '\r')"
fi
N="$(adb shell toybox date -d "$D +1 day" +%F 2>/dev/null | tr -d '\r')"

RECOMP() {
  adb exec-out run-as "$PKG" sh -c '
    set -eu; raw="'"$CSV_RAW"'"; d="$1"
    [ -f "$raw" ] || { echo 0; exit 0; }
    n=$(toybox date -d "$d +1 day" +%F 2>/dev/null) || n="$d"
    ws=$(toybox date -d "$d 22:00:00" +%s 2>/dev/null) || ws=0
    we=$(toybox date -d "$d 02:00:00 +1 day" +%s 2>/dev/null) || we=0
    acc=0; prev_ts=""; prev_on=0; last_on=0
    while IFS=, read -r ts state; do
      [ "$ts" = "ts" ] && continue
      t=$(toybox date -d "$ts" +%s 2>/dev/null) || continue
      case "$state" in
        *ON*|*Unlock*|*SCREEN_ON*|*SCREEN-ON*)  cur_on=1 ;;
        *OFF*|*LOCK*|*SCREEN_OFF*|*SCREEN-OFF*) cur_on=0 ;;
        * ) cur_on=0 ;;
      esac
      if [ "$t" -lt "$ws" ]; then last_on="$cur_on"; prev_ts="$t"; prev_on="$cur_on"; continue; fi
      [ -z "$prev_ts" ] && { prev_ts="$ws"; prev_on="$last_on"; }
      seg_end="$t"; [ "$seg_end" -gt "$we" ] && seg_end="$we"
      if [ "$prev_on" -eq 1 ]; then
        s="$prev_ts"; [ "$s" -lt "$ws" ] && s="$ws"
        e="$seg_end"; [ "$e" -gt "$we" ] && e="$we"
        [ "$e" -gt "$s" ] && acc=$((acc + e - s))
      fi
      prev_ts="$t"; prev_on="$cur_on"
      [ "$t" -ge "$we" ] && break
    done < "$raw"
    if [ -n "$prev_ts" ] && [ "$prev_on" -eq 1 ] && [ "$prev_ts" -lt "$we" ]; then
      s="$prev_ts"; [ "$s" -lt "$ws" ] && s="$ws"; e="$we"; [ "$e" -gt "$s" ] && acc=$((acc + e - s))
    fi
    echo $(( acc / 60 ))
  ' sh "$1"
}

DAILY_VAL() {
  adb exec-out run-as "$PKG" awk -F, -v d="$1" 'NR>1&&$1==d{print $3;exit}' "$CSV_DAILY" 2>/dev/null | tr -d '\r'
}

WS="$(adb shell toybox date -d "$D 22:00:00" +%s 2>/dev/null | tr -d '\r' || echo 0)"
WE="$(adb shell toybox date -d "$D 02:00:00 +1 day" +%s 2>/dev/null | tr -d '\r' || echo 0)"
B="$(RECOMP "$D")"
A_DAILY="$(DAILY_VAL "$D")"; [ -n "$A_DAILY" ] || A_DAILY=0

RAW_CNT="$(adb exec-out run-as "$PKG" awk -F, -v d="$D" -v n="$N" 'NR==1{next}{dd=substr($1,1,10); if(dd>=d && dd<=n) c++} END{print c+0}' "$CSV_RAW" 2>/dev/null | tr -d '\r' || echo 0)"
SNAP="$(adb exec-out run-as "$PKG" awk -F, -v d="$D" -v n="$N" 'NR==1{next}{dd=substr($1,1,10); if(dd>=d && dd<=n) print}' "$CSV_RAW" 2>/dev/null | tail -n 20 | tr -d '\r' || true)"

{
  echo "LNSU-DIAG TZ=$TZSTR DATE=$D NEXT=$N WS=$WS WE=$WE"
  echo "LNSU-DIAG RECOMP_MIN=$B DAILY_MIN=$A_DAILY RAW_EVENTS_WINDOW=$RAW_CNT"
  echo "LNSU-DIAG RAW_TAIL_START"
  printf '%s\n' "$SNAP"
  echo "LNSU-DIAG RAW_TAIL_END"
  echo "LNSU-DIAG RESULT=DONE"
} | tee "$OUT"
exit 0
