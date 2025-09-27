#!/usr/bin/env bash
set -euo pipefail

PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/_repo/csv_headers.audit.txt"
mkdir -p "$(dirname "$OUT")"

declare -A EXPECTED=(
  ["files/daily_unlocks.csv"]="date,count"
  ["files/unlock_log.csv"]="ts,event"
  ["files/daily_sleep_summary.csv"]="date,sleep_time,wake_time,duration_hours"
  ["files/daily_sleep_duration.csv"]="date,duration_hours"
  ["files/screen_log.csv"]="ts,event"
  ["files/daily_lnslu.csv"]="date,late_night_minutes"
  ["files/daily_late_night_screen_usage.csv"]="date,minutes"
)

normalize() {
  LC_ALL=C sed $'1s/^\xEF\xBB\xBF//;s/\r$//' \
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
  printf "%s\n" "$(printf '—%.0s' {1..150})"

  for f in "${!EXPECTED[@]}"; do
    exp="${EXPECTED[$f]}"
    raw_hdr="$(adb exec-out run-as "$PKG" sh -c 'head -n 1 "'"$f"'" 2>/dev/null' || true)"
    if [ -z "$raw_hdr" ]; then
      printf "%-44s | %-42s | %-42s | %s\n" "$f" "$exp" "MISSING" "NO"
      continue
    fi
    act_norm="$(printf '%s' "$raw_hdr" | normalize)"
    exp_norm="$(printf '%s\n' "$exp" | normalize)"
    match="NO"
    [ "$act_norm" = "$exp_norm" ] && match="YES"
    act_disp="$(printf '%s' "$raw_hdr" | LC_ALL=C sed $'1s/^\xEF\xBB\xBF//;s/\r$//')"
    printf "%-44s | %-42s | %-42s | %s\n" "$f" "$exp" "$act_disp" "$match"
  done
} | tee "$OUT"

{
  echo
  echo "RAW FIRST LINES:"
  for f in "${!EXPECTED[@]}"; do
    echo "---- $f ----"
    adb exec-out run-as "$PKG" sh -c 'head -n 1 "'"$f"'" 2>/dev/null || echo "[MISSING]"' \
      | LC_ALL=C sed $'1s/^\xEF\xBB\xBF//;s/\r$//'
  done
} | tee -a "$OUT"

exit 0
