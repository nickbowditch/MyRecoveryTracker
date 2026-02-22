#!/bin/sh
set -e
PKG=com.nick.myrecoverytracker
EXT_CSV=/sdcard/Android/data/$PKG/files/csv/daily_movement_intensity.csv
EXT_AUD=/sdcard/Android/data/$PKG/files/audit/audit.log

adb logcat -c
adb shell am broadcast -a $PKG.ACTION_RUN_MOVEMENT_INTENSITY -n $PKG/.TriggerReceiver >/dev/null 2>&1 || true
sleep 2
echo "---- LOGCAT (DataWriteManager & MovementIntensity) ----"
adb logcat -d | grep -i -E "DataWriteManager|preflight|write ✓|write ✗|audit|MovementIntensityDaily" || true

echo "---- EXTERNAL PATHS ----"
adb shell 'ls -la /sdcard/Android/data/'$PKG'/files 2>/dev/null || true'
adb shell 'ls -la /sdcard/Android/data/'$PKG'/files/csv 2>/dev/null || true'
adb shell 'ls -la /sdcard/Android/data/'$PKG'/files/audit 2>/dev/null || true'

echo "---- CSV TAIL (external) ----"
adb shell 'tail -n 5 '"$EXT_CSV"' 2>/dev/null || echo "(no external csv yet)"'

echo "---- AUDIT TAIL (external) ----"
adb shell 'tail -n 5 '"$EXT_AUD"' 2>/dev/null || echo "(no audit yet)"'
