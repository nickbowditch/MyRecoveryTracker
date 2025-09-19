#!/bin/bash
PKG="${PKG:-com.nick.myrecoverytracker}"
F="files/daily_metrics.csv"
LOCK="app/locks/daily_metrics.header"
adb exec-out run-as "$PKG" ls "$F" >/dev/null 2>&1 || { echo "FAIL: $F not found on device"; exit 1; }
HDR="$(adb exec-out run-as "$PKG" head -n1 "$F" | tr -d $'\r')"
[ -n "$HDR" ] || { echo "FAIL: $F header missing"; exit 1; }
need_cols=("date" "feature_schema_version" "late_night_screen_minutes")
for col in "${need_cols[@]}"; do
  echo "$HDR" | awk -F, -v c="$col" 'BEGIN{ok=0} {for(i=1;i<=NF;i++) if($i==c) ok=1} END{exit ok?0:1}' \
    || { echo "FAIL: required column missing: $col"; exit 1; }
done
[ -f "$LOCK" ] || { echo "FAIL: lock file $LOCK not found. Run :app:qaSealMetrics after intentional changes."; exit 1; }
LOCKED="$(tr -d $'\r' < "$LOCK")"
if [ "$HDR" != "$LOCKED" ]; then
  echo "FAIL: daily_metrics header drift detected."
  echo "sealed:  $LOCKED"
  echo "current: $HDR"
  exit 1
fi
echo "OK: daily_metrics present, has required columns, and header matches lock."
