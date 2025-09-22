#!/bin/sh
PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/lnsu/tc2.1.txt"
CSV_DAILY="files/daily_lnsu.csv"
CSV_RAW="files/screen_log.csv"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "TC-2 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "TC-2 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

DAYS="$(adb exec-out run-as "$PKG" awk -F, 'NR>1{d=$1} END{print d}' "$CSV_DAILY" 2>/dev/null | tr -d '\r')"
[ -n "$DAYS" ] || { echo "TC-2 RESULT=FAIL (no daily_lnsu.csv)" | tee "$OUT"; exit 4; }

recompute() {
  adb exec-out run-as "$PKG" sh -c '
    d="$1"; raw="'"$CSV_RAW"'"
    [ -f "$raw" ] || { echo 0; exit 0; }
    ws=$(toybox date -d "$d 22:00:00" +%s 2>/dev/null) || ws=0
    we=$(toybox date -d "$d 02:00:00 +1 day" +%s 2>/dev/null) || we=0
    acc=0; prev_ts=""; prev_on=0; last_on=0

    while IFS=, read -r ts state; do
      [ "$ts" = "ts" ] && continue
      t=$(toybox date -d "$ts" +%s 2>/dev/null) || continue
      case "$state" in
        *ON*|*Unlock*|*SCREEN_ON*|*SCREEN-ON* ) cur_on=1 ;;
        *OFF*|*LOCK*|*SCREEN_OFF*|*SCREEN-OFF* ) cur_on=0 ;;
        * ) cur_on=0 ;;
      esac

      if [ "$t" -lt "$ws" ]; then
        last_on="$cur_on"; prev_ts="$t"; prev_on="$cur_on"
        continue
      fi

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
      s="$prev_ts"; [ "$s" -lt "$ws" ] && s="$ws"
      e="$we"
      [ "$e" -gt "$s" ] && acc=$((acc + e - s))
    fi

    echo $(( acc / 60 ))
  ' sh "$1"
}

daily_val="$(adb exec-out run-as "$PKG" awk -F, -v d="$DAYS" 'NR>1&&$1==d{print $3;exit}' "$CSV_DAILY" 2>/dev/null | tr -d "\r")"
[ -n "$daily_val" ] || daily_val=0
recomp="$(recompute "$DAYS")"

if [ "$recomp" = "$daily_val" ]; then
  echo "TC-2 RESULT=PASS" | tee "$OUT"
  exit 0
else
  echo "TC-2 RESULT=FAIL ($DAYS daily=$daily_val recomputed=$recomp)" | tee "$OUT"
  exit 1
fi
