#!/bin/bash
PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/_repo/schema_sweep.1.txt"
adb get-state >/dev/null 2>&1 || { echo "SCHEMA-SWEEP RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "SCHEMA-SWEEP RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }
declare -a MAP
MAP+=("app/locks/daily_metrics.header|files/daily_unlocks.csv")
MAP+=("app/locks/daily_sleep.header|files/daily_sleep.csv")
for h in app/locks/*.header; do
  [ -f "$h" ] || continue
  b="${h##*/}"; b="${b%.header}"
  csv="files/$b.csv"
  found=0
  for p in "${MAP[@]}"; do IFS='|' read -r hh cc <<<"$p"; [ "$hh" = "$h" ] && { found=1; break; }; done
  [ $found -eq 1 ] || MAP+=("$h|$csv")
done
bad=0; det=""
for p in "${MAP[@]}"; do
  IFS='|' read -r LOCKF CSVF <<<"$p"
  LOCK="$(tr -d $'\r' < "$LOCKF" 2>/dev/null)"
  [ -n "$LOCK" ] || { bad=1; det="${det}MISSING_LOCK:$LOCKF"$'\n'; continue; }
  HEAD="$(adb exec-out run-as "$PKG" sh -c 'cat '"$CSVF"' 2>/dev/null | head -n 1' | tr -d $'\r')"
  [ -n "$HEAD" ] || { bad=1; det="${det}MISSING_CSV:$CSVF"$'\n'; continue; }
  if [ "$HEAD" != "$LOCK" ]; then
    bad=1
    det="${det}MISMATCH:$CSVF"$'\n'"LOCK:$LOCK"$'\n'"HEAD:$HEAD"$'\n'
  fi
done
if [ "$bad" -eq 0 ]; then
  echo "SCHEMA-SWEEP RESULT=PASS" | tee "$OUT"
  exit 0
else
  { echo "SCHEMA-SWEEP RESULT=FAIL"; printf "%s" "$det"; } | tee "$OUT"
  exit 1
fi
