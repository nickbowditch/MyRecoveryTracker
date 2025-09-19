#!/usr/bin/env zsh
PKG="${1:-com.nick.myrecoverytracker}"
ROOT="app/src"
csvs='daily_sleep_summary\.csv|daily_sleep_duration\.csv|daily_sleep_quality\.csv|daily_sleep_time\.csv|daily_wake_time\.csv'
writes='writeText|appendText|FileWriter|PrintWriter|FileOutputStream|openFileOutput|bufferedWriter|OutputStreamWriter'
hits=$(grep -RInE "($csvs).*(($writes))|(($writes)).*(($csvs))" "$ROOT" --include='*.kt' --include='*.java' 2>/dev/null | cut -d: -f1 | sort -u)
allow_re='SleepRollupWorker\.kt|LogRetentionWorker\.kt'
bad=$(echo "$hits" | grep -Ev "$allow_re" || true)
[ -n "$bad" ] && { echo "GUARDRAILS: FAIL writers"; echo "$bad"; exit 1; }
lock_bad=$(adb exec-out run-as "$PKG" sh -c 'ls files/*.lock 2>/dev/null || true')
[ -n "$lock_bad" ] && { echo "GUARDRAILS: FAIL lock_present $lock_bad"; exit 1; }
echo "GUARDRAILS: OK"
