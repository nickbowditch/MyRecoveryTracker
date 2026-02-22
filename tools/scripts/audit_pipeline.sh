#!/bin/sh
set -e
PKG="${PKG:-com.nick.myrecoverytracker}"
OUT="evidence/pipeline_audit_$(date -u +%Y%m%dT%H%M%SZ)"
EXT="/sdcard/Android/data/$PKG/files"
REDCAP_URL="${REDCAP_URL:-}"
REDCAP_TOKEN="${REDCAP_TOKEN:-}"
mkdir -p "$OUT"

# (a) SENSORS
adb shell dumpsys location > "$OUT/location_dumpsys.txt" 2>&1 || true
grep -n "$PKG" "$OUT/location_dumpsys.txt" > "$OUT/location_grep.txt" 2>/dev/null || true

# (b) ON-DEVICE CSVs
adb shell ls -l "$EXT" > "$OUT/files_ls.txt" 2>&1 || true
adb shell "ls $EXT/*.csv 2>/dev/null" > "$OUT/csv_list.txt" 2>/dev/null || true
adb shell "ls -l $EXT/*.csv 2>/dev/null" > "$OUT/csv_ls_l.txt" 2>/dev/null || true
adb shell "ls $EXT/*.csv 2>/dev/null | xargs -n1 sh -c 'echo --- \"\$1\"; tail -n 5 \"\$1\"' _ 2>/dev/null" \
  > "$OUT/csv_tails.txt" 2>/dev/null || true

# (c) JOBS
adb shell dumpsys jobscheduler | sed -n "/$PKG/,/DUMP OF ALL JOBS FINISHED/p" > "$OUT/jobs_dump.txt" 2>&1 || true

# (d)(e) REDCap
if [ -n "$REDCAP_URL" ] && [ -n "$REDCAP_TOKEN" ]; then
  curl -sS -X POST -d "token=$REDCAP_TOKEN" -d "content=version" "$REDCAP_URL" > "$OUT/redcap_version.txt" || true
  curl -sS -X POST -d "token=$REDCAP_TOKEN" -d "content=log" -d "format=json" \
    -d "beginTime=$(date -u -d '7 days ago' '+%Y-%m-%d %H:%M:%S' 2>/dev/null || date -u -v-7d '+%Y-%m-%d %H:%M:%S')" \
    -d "endTime=$(date -u '+%Y-%m-%d %H:%M:%S')" \
    "$REDCAP_URL" > "$OUT/redcap_logging.json" || true
  curl -sS -X POST -d "token=$REDCAP_TOKEN" -d "content=record" -d "format=count" \
    "$REDCAP_URL" > "$OUT/redcap_record_count.json" || true
fi

# SUMMARY
CSV_COUNT=$(wc -l < "$OUT/csv_list.txt" 2>/dev/null || echo 0)
SENSORS_PASS=$(test -s "$OUT/location_grep.txt" && echo PASS || echo FAIL)
JOBS_PASS=$(grep -q 'SystemJobService' "$OUT/jobs_dump.txt" 2>/dev/null && echo PASS || echo FAIL)
REDCAP_SUM="SKIP"
if [ -s "$OUT/redcap_record_count.json" ]; then
  CNT=$(sed -n 's/[^0-9]*\([0-9][0-9]*\).*/\1/p' "$OUT/redcap_record_count.json" | head -n1)
  [ -z "$CNT" ] && CNT="UNKNOWN"
  IMPORTS=$(grep -o '"action":"[^"]*"' "$OUT/redcap_logging.json" 2>/dev/null | grep -Eci 'Import|Record created|Upload' || echo 0)
  REDCAP_SUM="records=$CNT; recent_import_events=$IMPORTS"
fi

{
  echo "== PIPELINE AUDIT $(date -u '+%Y-%m-%dT%H:%M:%SZ') =="
  echo "(a) Sensors: $SENSORS_PASS"
  echo "(b) CSVs: $CSV_COUNT"
  echo "(c) Jobs: $JOBS_PASS"
  echo "(d)(e) REDCap: $REDCAP_SUM"
} > "$OUT/_summary.txt"

echo "$OUT"
