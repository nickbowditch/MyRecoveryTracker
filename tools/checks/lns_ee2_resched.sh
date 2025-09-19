#!/usr/bin/env bash
PKG="com.nick.myrecoverytracker"
F1="files/daily_late_screen.csv"
F2="files/daily_unlocks.csv"

mtime() {
  adb exec-out run-as "$PKG" sh -c "toybox stat -c %Y $1 2>/dev/null || busybox stat -c %Y $1 2>/dev/null || echo 0" | tr -d '\r'
}
today_has_row() {
  local f="$1"
  local d="$(adb shell 'date +%F' | tr -d '\r')"
  adb exec-out run-as "$PKG" sh -c "cat $f 2>/dev/null" | awk -F, -v d="$d" 'NR>1&&$1==d{f=1} END{print (f?1:0)}'
}

M1="$(mtime $F1)"; M2="$(mtime $F2)"
adb shell am broadcast -a android.intent.action.BOOT_COMPLETED -p "$PKG" >/dev/null 2>&1
sleep 1
adb shell am broadcast -a android.intent.action.PACKAGE_REPLACED --es "android.intent.extra.PACKAGES" "$PKG" >/dev/null 2>&1

t=0; ok=0
while [ $t -lt 90 ]; do
  sleep 3
  N1="$(mtime $F1)"; N2="$(mtime $F2)"
  R1="$(today_has_row $F1)"; R2="$(today_has_row $F2)"
  if [ "$N1" != "$M1" ] || [ "$N2" != "$M2" ] || [ "$R1" = "1" ] || [ "$R2" = "1" ]; then ok=1; break; fi
  t=$((t+3))
done

jobs_hit="$(adb shell dumpsys jobscheduler 2>/dev/null | grep -qi "$PKG" && echo hit || true)"
alarms_hit="$(adb shell dumpsys alarm 2>/dev/null | grep -qi "$PKG" && echo hit || true)"
wm_hit="$(adb shell dumpsys activity services 2>/dev/null | grep -qiE "$PKG|workmanager|androidx\.work" && echo hit || true)"

if [ $ok -eq 1 ] || [ -n "$jobs_hit$alarms_hit$wm_hit" ]; then
  echo "LNS EE-2 RESULT=PASS"
  exit 0
else
  echo "LNS EE-2 RESULT=FAIL"
  exit 1
fi
