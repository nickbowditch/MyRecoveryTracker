#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
adb get-state >/dev/null 2>&1 || exit 2
adb shell pm path "$PKG" >/dev/null 2>&1 || exit 3
adb exec-out run-as "$PKG" sh <<'IN'
set -eu
raw="files/screen_log.csv"
csv="files/daily_lnslu.csv"
lock="app/locks/daily_lnslu.header"
[ -f "$lock" ] || { mkdir -p app/locks; printf "date,late_night_minutes\n" > "$lock"; }
EXP="$(head -n1 "$lock" 2>/dev/null | tr -d '\r')"
if [ ! -f "$csv" ]; then mkdir -p "$(dirname "$csv")"; printf "%s\n" "$EXP" > "$csv"; fi
[ -f "$raw" ] || { echo "NO-RAW"; exit 0; }
D="$(awk -F, 'NR==1{next}{d=substr($1,1,10)} END{print d}' "$raw" 2>/dev/null | tr -d '\r')"
[ -n "$D" ] || { echo "NO-DATE"; exit 0; }
n="$(toybox date -d "$D +1 day" +%F 2>/dev/null || echo "$D")"
ws="$(toybox date -d "$D 22:00:00" +%s 2>/dev/null || echo 0)"
we="$(toybox date -d "$D 02:00:00 +1 day" +%s 2>/dev/null || echo 0)"
acc=0; prev_ts=""; prev_on=0; last_on=0
awk -F, -v d="$D" -v n="$n" 'NR==1{next}{dd=substr($1,1,10); if(dd<d) next; if(dd>n) exit; print}' "$raw" \
| while IFS=, read -r ts ev; do
  t="$(toybox date -d "$ts" +%s 2>/dev/null)" || continue
  case "$ev" in ON|Unlock|SCREEN_ON|SCREEN-ON) cur_on=1 ;; OFF|LOCK|SCREEN_OFF|SCREEN-OFF) cur_on=0 ;; *) cur_on=0 ;; esac
  if [ "$t" -lt "$ws" ]; then last_on="$cur_on"; prev_ts="$t"; prev_on="$cur_on"; continue; fi
  [ -z "$prev_ts" ] && { prev_ts="$ws"; prev_on="$last_on"; }
  seg_end="$t"; [ "$seg_end" -gt "$we" ] && seg_end="$we"
  if [ "$prev_on" -eq 1 ]; then s="$prev_ts"; [ "$s" -lt "$ws" ] && s="$ws"; e="$seg_end"; [ "$e" -gt "$we" ] && e="$we"; [ "$e" -gt "$s" ] && acc=$((acc + e - s)); fi
  prev_ts="$t"; prev_on="$cur_on"
  [ "$t" -ge "$we" ] && break
done
if [ -n "$prev_ts" ] && [ "$prev_on" -eq 1 ] && [ "$prev_ts" -lt "$we" ]; then s="$prev_ts"; [ "$s" -lt "$ws" ] && s="$ws"; e="$we"; [ "$e" -gt "$s" ] && acc=$((acc + e - s)); fi
mins=$(( acc / 60 ))
hdr="$(head -n1 "$csv" | tr -d '\r')"
[ "$hdr" = "$EXP" ] || { tmp="${csv}.tmp.$$"; { echo "$EXP"; tail -n +2 "$csv"; } > "$tmp"; mv "$tmp" "$csv"; }
tmp="${csv}.tmp.$$"
awk -F, -v OFS="," -v d="$D" -v v="$mins" 'NR==1{print; seen=0; next} { if($1==d){$2=v; seen=1} ; print } END{ if(seen==0) print d,v }' "$csv" > "$tmp"
mv "$tmp" "$csv"
echo "$D,$mins"
IN
