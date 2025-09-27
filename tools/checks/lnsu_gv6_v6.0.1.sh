#!/bin/sh
set -eu

APP="${APP:-com.nick.myrecoverytracker}"
CSV="files/daily_lnslu.csv"
LOCK="app/locks/di.lnslu.lock"
OUT="evidence/v6.0/lnsu/gv6.txt"
mkdir -p "$(dirname "$OUT")"

[ -f "$LOCK" ] || { echo "GV6 RESULT=FAIL (missing $LOCK)" | tee "$OUT"; exit 1; }
grep -qx 'file=files/daily_lnslu.csv' "$LOCK" || { echo "GV6 RESULT=FAIL (lock file path mismatch)" | tee "$OUT"; exit 1; }
grep -qx 'min=0' "$LOCK" || { echo "GV6 RESULT=FAIL (lock min mismatch)" | tee "$OUT"; exit 1; }
grep -qx 'max=240' "$LOCK" || { echo "GV6 RESULT=FAIL (lock max mismatch)" | tee "$OUT"; exit 1; }

adb shell run-as "$APP" sh -c '[ -f "'"$CSV"'" ]' || { echo "GV6 RESULT=FAIL (missing $CSV on device)" | tee "$OUT"; exit 1; }

if adb exec-out run-as "$APP" cat "$CSV" 2>/dev/null | awk -F',' '
  BEGIN{ok=1}
  NR==1{
    h1=$1; gsub(/^[ \t]+|[ \t]+$/,"",h1)
    if (h1!="date") { ok=0; exit }
    next
  }
  NR>1{
    v=$2; gsub(/^[ \t]+|[ \t]+$/,"",v); sub(/\r$/,"",v)
    if (v=="" || v !~ /^-?[0-9]+(\.[0-9]+)?$/) { ok=0; exit }
    if (v+0 < 0 || v+0 > 240) { ok=0; exit }
  }
  END{exit ok?0:1}
'; then
  echo "GV6 RESULT=PASS" | tee "$OUT"; exit 0
fi

echo "GV6 RESULT=FAIL (bounds/header validation)" | tee "$OUT"; exit 1
