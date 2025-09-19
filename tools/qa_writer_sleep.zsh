#!/usr/bin/env zsh
ROOT="app/src"
S=0
csvs='daily_sleep_summary\.csv|daily_sleep_duration\.csv|daily_sleep_quality\.csv|daily_sleep_time\.csv|daily_wake_time\.csv'
writes='writeText|appendText|FileWriter|PrintWriter|FileOutputStream|openFileOutput|bufferedWriter|OutputStreamWriter'

hits=$(grep -RInE "($csvs).*(($writes))|(($writes)).*(($csvs))" "$ROOT" --include='*.kt' --include='*.java' 2>/dev/null | cut -d: -f1 | sort -u)
allow_re='SleepRollupWorker\.kt|LogRetentionWorker\.kt'

bad=$(echo "$hits" | grep -Ev "$allow_re" || true)

if [ -n "$bad" ]; then
  echo "FAIL: non-allowed writers:"
  echo "$bad"
  exit 1
fi

echo "WRITER: OK"
echo "$hits"
exit 0
