#!/bin/sh
PKG="com.nick.myrecoverytracker"
CSV_DAILY="files/daily_unlocks.csv"
CSV_RAW="files/unlock_log.csv"
HDR="date,feature_schema_version,daily_unlocks"
TODAY="$(date -u +%F)"
adb get-state >/dev/null 2>&1 || exit 2
adb shell pm path "$PKG" >/dev/null 2>&1 || exit 3
adb exec-out run-as "$PKG" sh -c '
set -eu
d="'"$CSV_DAILY"'"; r="'"$CSV_RAW"'"; h="'"$HDR"'"; today="'"$TODAY"'"
mkdir -p files
[ -f "$r" ] || printf "ts,event\n" > "$r"
tmp="${d}.tmp.$$"
{
  printf "%s\n" "$h"
  tail -n +2 "$r" | cut -d, -f1 | cut -c1-10 \
  | grep -E "^[0-9]{4}-[0-9]{2}-[0-9]{2}$" \
  | awk -v t="$today" '"'"'$0<=t'"'"' \
  | sort | uniq -c \
  | while read c dt; do [ -n "$dt" ] && printf "%s,v6.0,%s\n" "$dt" "$c"; done \
  | sort
} > "$tmp"
mv "$tmp" "$d"
'
