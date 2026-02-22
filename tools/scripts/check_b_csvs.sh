#!/bin/sh
set -e
PKG=${PKG:-com.nick.myrecoverytracker}
APPDIR="/sdcard/Android/data/$PKG"
OUT="evidence/b_csvs_$(date -u +%Y%m%dT%H%M%SZ)"
mkdir -p "$OUT"

# 1) App external dir tree + CSVs there (root + common subdirs)
adb shell ls -la "$APPDIR" > "$OUT/appdir_root.txt" 2>&1 || true
adb shell ls -la "$APPDIR/files" > "$OUT/appdir_files.txt" 2>&1 || true
adb shell ls -la "$APPDIR/files/csv" > "$OUT/appdir_files_csv.txt" 2>&1 || true
adb shell ls -la "$APPDIR/files/logs" > "$OUT/appdir_files_logs.txt" 2>&1 || true

# Collect mtimes and tails for any CSVs under files/**
adb shell 'for f in '"$APPDIR"'/files/**/*.csv '"$APPDIR"'/files/*.csv; do [ -f "$f" ] && { printf "%s\t" "$f"; stat -c "%y" "$f" 2>/dev/null || stat -f "%Sm" -t "%Y-%m-%d %H:%M:%S" "$f"; }; done' \
  > "$OUT/appdir_csv_mtimes.tsv" 2>&1 || true
adb shell 'for f in '"$APPDIR"'/files/**/*.csv '"$APPDIR"'/files/*.csv; do [ -f "$f" ] && { echo "--- $f"; tail -n 5 "$f"; }; done' \
  > "$OUT/appdir_csv_tails.txt" 2>&1 || true

# 2) Any CSVs elsewhere on shared storage (slightly wider net)
adb shell 'find /sdcard -maxdepth 6 -type f -name "*.csv" 2>/dev/null | head -n 400' \
  > "$OUT/find_sdcard_csvs.txt" || true
adb shell 'ls -la /sdcard/Documents 2>/dev/null; ls -la /sdcard/Download 2>/dev/null' \
  > "$OUT/docs_downloads_ls.txt" 2>&1 || true

# 3) Permissions snapshot
adb shell dumpsys package "$PKG" | sed -n '/grantedPermissions/,/User 0/p' \
  > "$OUT/granted_perms.txt" || true
adb shell dumpsys appops | sed -n "/$PKG/,+24p" \
  > "$OUT/appops.txt" || true

# 4) Trigger a write path again and capture package-only logs for 20s
adb logcat -c
adb shell am broadcast -a com.nick.myrecoverytracker.ACTION_RUN_MOVEMENT_INTENSITY -n "$PKG"/.TriggerReceiver >/dev/null 2>&1 || true
PID=$(adb shell pidof "$PKG" 2>/dev/null | tr -d '\r' || true)
if [ -n "$PID" ]; then
  timeout 20s adb logcat -v time --pid="$PID" > "$OUT/pid_log.txt" || true
else
  : > "$OUT/pid_log.txt"
fi

# 5) Verdict for (b)
CSV_COUNT=$(grep -c '\.csv' "$OUT/appdir_csv_mtimes.tsv" 2>/dev/null || echo 0)
CSV_COUNT=${CSV_COUNT:-0}
# Count those "recent" in the last 14 days (string compare is fine on ISO timestamps we emit)
RECENT=$(awk -F'\t' '
  BEGIN{cutoff="2025-10-14"}
  $2 >= cutoff {rc++}
  END{print (rc?rc:0)}
' "$OUT/appdir_csv_mtimes.tsv" 2>/dev/null)
RECENT=${RECENT:-0}

SUMMARY="$OUT/_b_summary.txt"
{
  echo "DIR=$OUT"
  if [ "$CSV_COUNT" -gt 0 ] && [ "$RECENT" -gt 0 ]; then
    echo "(b) CSVs: PASS ($CSV_COUNT present; $RECENT updated in last 14 days)"
  elif [ "$CSV_COUNT" -gt 0 ]; then
    echo "(b) CSVs: PARTIAL ($CSV_COUNT present; none updated in last 14 days)"
  else
    echo "(b) CSVs: FAIL (no CSVs under $APPDIR/files/**)"
  fi
} > "$SUMMARY"

echo "$OUT"
