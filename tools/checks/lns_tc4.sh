#!/usr/bin/env bash
PKG="com.nick.myrecoverytracker"

range_dates() {
  adb exec-out run-as "$PKG" awk -F, 'NR==2{min=max=$1} NR>1{if($1<min)min=$1; if($1>max)max=$1} END{print min","max}' files/daily_late_screen.csv 2>/dev/null
}
find_dst_boundary_between() {
  local start="$1" end="$2"
  [ -z "$start" ] || [ -z "$end" ] && return 1
  local se="$(adb shell "toybox date -d '$start 12:00:00' +%s" | tr -d '\r')"
  local ee="$(adb shell "toybox date -d '$end 12:00:00' +%s" | tr -d '\r')"
  [ -z "$se" ] || [ -z "$ee" ] && return 1
  local prev="" t="$se"
  while [ "$t" -le "$ee" ]; do
    d="$(adb shell "toybox date -d '@$t' +%F" | tr -d '\r')"
    off="$(adb shell "toybox date -d '$d 12:00:00' +%z" | tr -d '\r')"
    if [ -n "$prev" ] && [ "$off" != "$prev" ]; then
      d_prev="$(adb shell "toybox date -d '@$((t-86400))' +%F" | tr -d '\r')"
      echo "$d_prev,$d"; return 0
    fi
    prev="$off"; t=$((t+86400))
  done
  return 1
}

read MINMAX <<<"$(range_dates)"
D1="${MINMAX%,*}"; D2="${MINMAX#*,}"
[ -n "$D1" ] && [ -n "$D2" ] || { echo "LNS TC-4 RESULT=SKIP (no data)"; exit 0; }
PAIR="$(find_dst_boundary_between "$D1" "$D2")"
[ -z "$PAIR" ] && { echo "LNS TC-4 RESULT=SKIP (no DST boundary in data range)"; exit 0; }

B1="${PAIR%,*}"; B2="${PAIR#*,}"
get_minutes() { adb exec-out run-as "$PKG" awk -F, -v dd="$1" 'NR>1&&$1==dd{print $2; exit}' files/daily_late_screen.csv 2>/dev/null; }
m1="$(get_minutes "$B1")"; m2="$(get_minutes "$B2")"

if [ -z "$m1" ] && [ -z "$m2" ]; then
  echo "LNS TC-4 RESULT=FAIL (no rows at boundary: $B1,$B2)"; exit 1
fi
ok=1
for v in "$m1" "$m2"; do
  [ -z "$v" ] && continue
  case "$v" in ''|*[!0-9]*) ok=0;; *) [ "$v" -ge 0 ] && [ "$v" -le 1440 ] || ok=0;; esac
done
[ $ok -eq 1 ] && echo "LNS TC-4 RESULT=PASS (boundary=$B1->$B2 m1=${m1:-∅} m2=${m2:-∅})" || { echo "LNS TC-4 RESULT=FAIL (out of range at boundary)"; exit 1; }
