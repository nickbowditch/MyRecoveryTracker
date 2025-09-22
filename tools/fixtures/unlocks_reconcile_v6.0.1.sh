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
[ -f "$r" ] || { printf "ts,event\n" > "$r"; }
tmp="${d}.tmp.$$"
{
  printf "%s\n" "$h"
  awk -F, -v t=today '
    BEGIN{OFS=","}
    NR==1{next}
    NR>1{
      dd=substr($1,1,10)
      if (dd!="" && dd<=t) rc[dd]++
    }
    END{
      n=asorti(rc,keys)
      for(i=1;i<=n;i++){
        d=keys[i]
        printf "%s,%s,%d\n", d, "v6.0", rc[d]+0
      }
    }
  ' "$r" | sort
} > "$tmp"
mv "$tmp" "$d"
'
