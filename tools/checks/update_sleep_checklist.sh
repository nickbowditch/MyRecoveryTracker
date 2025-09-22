#!/bin/sh
set -eu
CHK="app/checklist_v6.0.txt"
BACK="app/checklist_v6.0.txt.bak.$(date +%s)"

cp "$CHK" "$BACK"

awk '
BEGIN { in_sleep=0 }
{
  if ($0 ~ /^Sleep/) { print; in_sleep=1; next }
  if (in_sleep && $0 ~ /^-/) {
    if ($0 ~ /daily_sleep.csv/) next
  }
  if (in_sleep && NF==0) {
    print "- daily_sleep_summary.csv  (locked: app/locks/daily_sleep_summary.header)"
    print "- daily_sleep_duration.csv (locked: app/locks/daily_sleep_duration.header)"
    in_sleep=0
  }
  print
}
END {
  if (in_sleep) {
    print "- daily_sleep_summary.csv  (locked: app/locks/daily_sleep_summary.header)"
    print "- daily_sleep_duration.csv (locked: app/locks/daily_sleep_duration.header)"
  }
}
' "$CHK" > "$CHK.new"

mv "$CHK.new" "$CHK"

echo "Checklist updated. Backup at $BACK"
