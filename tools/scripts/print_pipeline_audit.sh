#!/bin/sh
set -e
DIR="${1:-$(ls -dt evidence/pipeline_audit_* 2>/dev/null | head -n1)}"
[ -n "$DIR" ] || { echo "NO_AUDITS"; exit 2; }

echo "$DIR"
sed -n '1,200p' "$DIR/_summary.txt" 2>/dev/null || true
[ -s "$DIR/csv_list.txt" ] && { echo "---- CSV LIST ----"; sed -n '1,200p' "$DIR/csv_list.txt"; }
[ -s "$DIR/csv_ls_l.txt" ] && { echo "---- CSV LS -L ----"; sed -n '1,200p' "$DIR/csv_ls_l.txt"; }
[ -s "$DIR/csv_tails.txt" ] && { echo "---- CSV TAILS ----"; sed -n '1,400p' "$DIR/csv_tails.txt"; }
[ -s "$DIR/location_grep.txt" ] && { echo "---- LOCATION (GREP) ----"; sed -n '1,200p' "$DIR/location_grep.txt"; }
[ -s "$DIR/jobs_dump.txt" ] && { echo "---- JOBS DUMP ----"; sed -n '1,200p' "$DIR/jobs_dump.txt"; }
[ -s "$DIR/redcap_version.txt" ] && { echo "---- REDCAP VERSION ----"; sed -n '1,80p' "$DIR/redcap_version.txt"; }
[ -s "$DIR/redcap_record_count.json" ] && { echo "---- REDCAP RECORD COUNT ----"; cat "$DIR/redcap_record_count.json"; }
[ -s "$DIR/redcap_logging.json" ] && { echo "---- REDCAP LOGGING (LAST 2000 BYTES) ----"; tail -c 2000 "$DIR/redcap_logging.json"; }
