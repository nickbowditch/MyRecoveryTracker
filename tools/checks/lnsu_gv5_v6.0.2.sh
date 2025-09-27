#!/bin/sh
set -eu
OUT="evidence/v6.0/lnsu/gv5.txt"
LOCK="app/locks/work.unique_names.lock"
mkdir -p "$(dirname "$OUT")"
[ -f "$LOCK" ] || { echo "GV5 RESULT=FAIL (missing $LOCK)" | tee "$OUT"; exit 1; }

if awk 'BEGIN{once=0;per=0}
BEGINFILE{FS=","}
{
  a=$1; gsub(/^[ \t]+|[ \t]+$/,"",a)
  b=$2; gsub(/^[ \t]+|[ \t]+$/,"",b); sub(/\r$/,"",b)
  if(a=="once-LateNightRollup" && b=="ExistingWorkPolicy.REPLACE") once=1
  if(a=="periodic_late_night_rollup" && b=="ExistingPeriodicWorkPolicy.UPDATE") per=1
}
END{exit !(once && per)}' "$LOCK"
then
  echo "GV5 RESULT=PASS" | tee "$OUT"
  exit 0
fi

echo "GV5 RESULT=FAIL" | tee "$OUT"
exit 1
