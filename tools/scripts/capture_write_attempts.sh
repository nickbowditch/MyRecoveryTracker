#!/bin/sh
set -e
PKG=${PKG:-com.nick.myrecoverytracker}
OUT="evidence/write_attempts_$(date -u +%Y%m%dT%H%M%SZ).log"
adb logcat -c
adb shell am broadcast -a com.nick.myrecoverytracker.ACTION_RUN_MOVEMENT_INTENSITY -n com.nick.myrecoverytracker/.TriggerReceiver >/dev/null 2>&1 || true
timeout 60s adb logcat -v time | grep -i -E "$PKG|csv|write|storage|IOException|EACCES|denied" > "$OUT" || true
echo "$OUT"
