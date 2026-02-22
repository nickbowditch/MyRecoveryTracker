#!/bin/bash
PKG=com.nick.myrecoverytracker
OUT="evidence/force_jobs_$(date -u +%Y%m%dT%H%M%SZ)"
mkdir -p "$OUT"

adb shell dumpsys jobscheduler | sed -n "/$PKG/,/DUMP OF ALL JOBS FINISHED/p" > "$OUT/jobs_dump.txt"
IDS=$(grep -oE '#u0a[0-9]+/([0-9]+)' "$OUT/jobs_dump.txt" | sed -E 's#.*/##' | sort -u)

for id in $IDS; do
  adb shell cmd jobscheduler run -f "$PKG" "$id" >/dev/null 2>&1 || true
done

adb shell monkey -p "$PKG" -c android.intent.category.DEFAULT 1 || true
adb shell am broadcast -a android.intent.action.MY_PACKAGE_REPLACED -p "$PKG" || true
adb shell am broadcast -a android.intent.action.BOOT_COMPLETED -p "$PKG" || true

sleep 5
EXT="/sdcard/Android/data/$PKG/files"
adb shell ls -l "$EXT" > "$OUT/ext_ls.txt" 2>&1
adb shell sh -c "for f in $EXT/*.csv; do [ -f \"\$f\" ] && { echo \"--- \$f\"; tail -n 5 \"\$f\"; }; done" > "$OUT/ext_tail.txt" 2>&1
echo "$OUT"
