#!/bin/sh
set -eu

PKG="com.nick.myrecoverytracker"
STAMP="$(date +%Y%m%dT%H%M%S)"
OUTDIR="evidence/validation/$STAMP"
mkdir -p "$OUTDIR"

adb get-state >/dev/null

adb shell pm grant "$PKG" android.permission.POST_NOTIFICATIONS || true
adb shell pm grant "$PKG" android.permission.ACCESS_FINE_LOCATION || true
adb shell pm grant "$PKG" android.permission.ACCESS_COARSE_LOCATION || true
adb shell pm grant "$PKG" android.permission.ACTIVITY_RECOGNITION || true
adb shell cmd appops set "$PKG" GET_USAGE_STATS allow || true
adb shell dumpsys deviceidle whitelist +"$PKG" >/dev/null 2>&1 || true

adb shell dumpsys package "$PKG" > "$OUTDIR/package_dumpsys.txt" || true
grep -E 'ACCESS_(FINE|COARSE)_LOCATION|ACTIVITY_RECOGNITION|POST_NOTIFICATIONS|RECEIVE_BOOT_COMPLETED' \
  "$OUTDIR/package_dumpsys.txt" > "$OUTDIR/permissions_summary.txt" || true

adb shell dumpsys notification --noredact > "$OUTDIR/notification_dump.txt" || true
grep -n "$PKG" "$OUTDIR/notification_dump.txt" > "$OUTDIR/notification_grep.txt" || true

adb shell dumpsys activity service WorkManager > "$OUTDIR/workmanager_dump.txt" 2>/dev/null || true
adb shell dumpsys jobscheduler | awk "/$PKG/{flag=1} flag{print} /DUMP OF ALL JOBS FINISHED/{flag=0}" \
  > "$OUTDIR/jobscheduler_dump.txt" 2>/dev/null || true

adb shell dumpsys location > "$OUTDIR/location_dump.txt" || true
grep -n "$PKG" "$OUTDIR/location_dump.txt" > "$OUTDIR/location_grep.txt" || true

EXT_DIR="/sdcard/Android/data/$PKG/files"
adb shell ls -l "$EXT_DIR" > "$OUTDIR/files_ls.txt" 2>&1 || true
adb shell sh -c "for f in $EXT_DIR/*.csv; do [ -f \"\$f\" ] && { echo \"--- \$f\"; tail -n 5 \"\$f\"; }; done" \
  > "$OUTDIR/files_csv_tail.txt" 2>&1 || true

{
  echo "== PERMISSIONS =="
  cat "$OUTDIR/permissions_summary.txt" 2>/dev/null || true
  echo
  echo "== NOTIFICATIONS =="
  cat "$OUTDIR/notification_grep.txt" 2>/dev/null || true
  echo
  echo "== JOBS (WorkManager) =="
  wc -l "$OUTDIR/workmanager_dump.txt" 2>/dev/null || true
  echo
  echo "== JOBS (JobScheduler) =="
  head -n 80 "$OUTDIR/jobscheduler_dump.txt" 2>/dev/null || true
  echo
  echo "== LOCATION =="
  head -n 80 "$OUTDIR/location_grep.txt" 2>/dev/null || true
  echo
  echo "== CSV FILES =="
  if grep -q '\.csv' "$OUTDIR/files_ls.txt" 2>/dev/null; then
    echo "CSV(s) detected under $EXT_DIR"
  else
    echo "No CSVs detected yet under $EXT_DIR"
  fi
} > "$OUTDIR/_summary.txt" 2>/dev/null || true

echo "✅ $OUTDIR"
