#!/bin/sh
set -e
DIR="${1:-$(ls -dt evidence/pipeline_audit_* 2>/dev/null | head -n1)}"
[ -n "$DIR" ] || { echo "NO_AUDITS"; exit 2; }
echo "$DIR"
echo "---- SUMMARY ----"; sed -n '1,200p' "$DIR/_summary.txt" 2>/dev/null || true
echo "---- LOCATION (grep) ----"; sed -n '1,120p' "$DIR/location_grep.txt" 2>/dev/null || true
echo "---- FILES (ls) ----"; sed -n '1,120p' "$DIR/files_ls.txt" 2>/dev/null || true
echo "---- CSV MTIMES ----"; sed -n '1,200p' "$DIR/csv_mtimes.tsv" 2>/dev/null || true
echo "---- CSV TAILS ----"; sed -n '1,200p' "$DIR/csv_tails.txt" 2>/dev/null || true
echo "---- JOBS DUMP (first 200) ----"; sed -n '1,200p' "$DIR/jobs_dump.txt" 2>/dev/null || true
[ -f "$DIR/redcap_version.txt" ] && { echo "---- REDCAP VERSION ----"; sed -n '1,60p' "$DIR/redcap_version.txt"; }
[ -f "$DIR/redcap_record_count.json" ] && { echo "---- REDCAP RECORD COUNT ----"; cat "$DIR/redcap_record_count.json"; }
[ -f "$DIR/redcap_logging.json" ] && { echo "---- REDCAP LOGGING (last 2000 bytes) ----"; tail -c 2000 "$DIR/redcap_logging.json"; }
