#!/bin/sh
set -eu

PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/_repo/csv_headers.audit.txt"
mkdir -p "$(dirname "$OUT")"

normalize() {
  LC_ALL=C sed -e '1s/^\xEF\xBB\xBF//' -e 's/\r$//' \
  | tr '[:upper:]' '[:lower:]' \
  | awk -F',' '{
      for(i=1;i<=NF;i++){ gsub(/^[ \t]+|[ \t]+$/,"",$i) }
      OFS=","; print $0
    }'
}

adb get-state >/dev/null 2>&1 || { echo "ERROR: no device/emulator" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "ERROR: app not installed ($PKG)" | tee "$OUT"; exit 3; }

{
  printf "CSV HEADER AUDIT (%s)\n" "$(date -u +'%F %T UTC')"
  printf "Package: %s\n\n" "$PKG"
  printf "%-44s | %-42s | %-42s | %s\n" "FILE" "EXPECTED" "ACTUAL" "MATCH"
  i=1; while [ $i -le 150 ]; do printf "—"; i=$((i+1)); done; printf "\n"

  while IFS='|' read -r FILE EXP; do
    [ -n "$FILE" ] || continue
    RAW="$(adb exec-out run-as "$PKG" sh -c 'head -n 1 '"$FILE"' 2>/dev/null' || true)"
    if [ -z "$RAW" ]; then
      printf "%-44s | %-42s | %-42s | %s\n" "$FILE" "$EXP" "MISSING" "NO"
      continue
    fi
    ACT_N="$(printf '%s' "$RAW" | normalize)"
    EXP_N="$(printf '%s\n' "$EXP" | normalize)"
    MATCH="NO"; [ "$ACT_N" = "$EXP_N" ] && MATCH="YES"
    ACT_DISP="$(printf '%s' "$RAW" | LC_ALL=C sed -e '1s/^\xEF\xBB\xBF//' -e 's/\r$//')"
    printf "%-44s | %-42s | %-42s | %s\n" "$FILE" "$EXP" "$ACT_DISP" "$MATCH"
  done <<'MAP'
files/daily_unlocks.csv|date,count
files/unlock_log.csv|ts,event
files/daily_sleep_summary.csv|date,sleep_time,wake_time,duration_hours
files/daily_sleep_duration.csv|date,duration_hours
files/screen_log.csv|ts,event
files/daily_lnslu.csv|date,late_night_minutes
files/daily_late_night_screen_usage.csv|date,minutes
MAP

  echo
  echo "RAW FIRST LINES:"
  while IFS='|' read -r FILE EXP; do
    [ -n "$FILE" ] || continue
    echo "---- $FILE ----"
    adb exec-out run-as "$PKG" sh -c 'head -n 1 '"$FILE"' 2>/dev/null || echo "[MISSING]"' \
      | LC_ALL=C sed -e '1s/^\xEF\xBB\xBF//' -e 's/\r$//'
  done <<'MAP'
files/daily_unlocks.csv|date,count
files/unlock_log.csv|ts,event
files/daily_sleep_summary.csv|date,sleep_time,wake_time,duration_hours
files/daily_sleep_duration.csv|date,duration_hours
files/screen_log.csv|ts,event
files/daily_lnslu.csv|date,late_night_minutes
files/daily_late_night_screen_usage.csv|date,minutes
MAP
} | tee "$OUT"

exit 0
