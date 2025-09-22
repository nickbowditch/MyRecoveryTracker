#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/lnsu/seed_daily_from_raw.2.txt"
LOCK="app/locks/daily_lnsu.header"
CSV_DAILY="files/daily_lnsu.csv"
CSV_RAW="files/screen_log.csv"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "LNSU-SEED RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "LNSU-SEED RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

EXP="$(tr -d '\r' < "$LOCK" 2>/dev/null || true)"
[ -n "$EXP" ] || { echo "LNSU-SEED RESULT=FAIL (missing lock)" | tee "$OUT"; exit 4; }

D="$(adb shell toybox date -d '@$(( $(adb shell toybox date +%s | tr -d "\r") - 86400 ))' +%F | tr -d '\r')"

adb exec-out run-as "$PKG" sh -c '
set -eu
exp="'"$EXP"'"; d="'"$D"'"
daily="'"$CSV_DAILY"'"; raw="'"$CSV_RAW"'"
mkdir -p files

# ensure daily file + header matches lock
if [ -f "$daily" ]; then
  cur="$(head -n1 "$daily" 2>/dev/null | tr -d "\r")"
  if [ "$cur" != "$exp" ]; then
    tmp="${daily}.tmp.$$"; { echo "$exp"; tail -n +2 "$daily"; } > "$tmp" && mv "$tmp" "$daily"
  fi
else
  echo "$exp" > "$daily"
fi

# ensure raw exists (create empty with header if missing)
[ -f "$raw" ] || echo "ts,state" > "$raw"

# recompute minutes in 22:00–02:00 for date d
mins=$(
  ws=$(toybox date -d "$d 22:00:00" +%s 2>/dev/null) || ws=0
  we=$(toybox date -d "$d 02:00:00 +1 day" +%s 2>/dev/null) || we=0
  acc=0; prev_ts=""; prev_on=0; last_on=0
  while IFS=, read -r ts state; do
    [ "$ts" = "ts" ] && continue
    t=$(toybox date -d "$ts" +%s 2>/dev/null) || continue
    case "$state" in
      *ON*|*Unlock*|*SCREEN_ON*|*SCREEN-ON*)  cur_on=1 ;;
      *OFF*|*LOCK*|*SCREEN_OFF*|*SCREEN-OFF*) cur_on=0 ;;
      *) cur_on=0 ;;
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
    s="$prev_ts"; [ "$s" -lt "$ws" ] && s="$ws"
    e="$we"; [ "$e" -gt "$s" ] && acc=$((acc + e - s))
  fi
  echo $(( acc / 60 ))
)

# append row only if missing for D
awk -F, -v d="$D" "NR>1&&\$1==d{f=1} END{exit f?0:1}" "$daily" || \
  printf "%s,%s,%s\n" "$D" "v6.0" "$mins" >> "$daily"
' >/dev/null 2>&1

echo "LNSU-SEED RESULT=PASS (date=$D)" | tee "$OUT"
