#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
CSV="files/daily_lnslu.csv"
RAW="files/screen_log.csv"

D="$(adb exec-out run-as "$PKG" awk -F, 'NR>1{d=$1} END{print d}' "$CSV" 2>/dev/null | tr -d '\r' || true)"
[ -n "${D:-}" ] || { echo "No rows to repair"; exit 1; }

RECOMP="$(adb exec-out run-as "$PKG" sh -c '
set -eu; raw="'"$RAW"'"; d="'"$D"'"
[ -f "$raw" ] || { echo 0; exit 0; }
n=$(toybox date -d "$d +1 day" +%F 2>/dev/null || echo "$d")
ws=$(toybox date -d "$d 22:00:00" +%s 2>/dev/null || echo 0)
we=$(toybox date -d "$d 02:00:00 +1 day" +%s 2>/dev/null || echo 0)
acc=0; prev_ts=""; prev_on=0; last_on=0
awk -F, -v d="$d" -v n="$n" "NR==1{next}{dd=substr(\$1,1,10); if(dd<d) next; if(dd>n) exit; print}" "$raw" \
| while IFS=, read -r ts state; do
  t=$(toybox date -d "$ts" +%s 2>/dev/null) || continue
  case "$state" in
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

adb exec-out run-as "$PKG" sh -c '
set -eu
csv="'"$CSV"'"; d="'"$D"'"; v="'"$RECOMP"'"
bak="${csv}.bak.$(toybox date +%s)"
cp "$csv" "$bak" || :
{ head -n1 "$csv"; awk -F, -v d="$d" -v v="$v" "NR>1{ if(\$1==d){print d\",\"v} else {print} }" "$csv" | sed -n "2,\$p"; } > "${csv}.tmp.$$"
mv "${csv}.tmp.$$" "$csv"
echo "$d -> $v"
'
