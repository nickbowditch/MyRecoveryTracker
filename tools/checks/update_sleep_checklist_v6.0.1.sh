#!/bin/sh
set -eu
CHK="app/checklist_v6.0.txt"
TMP="$CHK.new"
TS="$(date +%s)"
mkdir -p app
if [ ! -f "$CHK" ]; then
  {
    echo "Checklist v6.0"
    echo
    echo "Sleep"
    echo "- daily_sleep_summary.csv  (locked: app/locks/daily_sleep_summary.header)"
    echo "- daily_sleep_duration.csv (locked: app/locks/daily_sleep_duration.header)"
    echo
  } > "$CHK"
  exit 0
fi
cp "$CHK" "$CHK.bak.$TS"
awk '
BEGIN{in_sleep=0;printed=0}
{
  if($0 ~ /^[[:space:]]*Sleep[[:space:]]*$/){ print; in_sleep=1; next }
  if(in_sleep){
    if($0 ~ /^-/) next
    if($0 ~ /^[[:space:]]*$/){
      if(!printed){
        print "- daily_sleep_summary.csv  (locked: app/locks/daily_sleep_summary.header)"
        print "- daily_sleep_duration.csv (locked: app/locks/daily_sleep_duration.header)"
        printed=1
      }
      print
      in_sleep=0
      next
    }
  }
  print
}
END{
  if(in_sleep && !printed){
    print "- daily_sleep_summary.csv  (locked: app/locks/daily_sleep_summary.header)"
    print "- daily_sleep_duration.csv (locked: app/locks/daily_sleep_duration.header)"
    print ""
  }
}
' "$CHK" > "$TMP"
mv "$TMP" "$CHK"
