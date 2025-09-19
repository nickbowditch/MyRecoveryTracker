#!/usr/bin/env bash
PKG="com.nick.myrecoverytracker"
F="files/daily_late_screen.csv"
discover_action() {
  adb shell dumpsys package "$PKG" 2>/dev/null \
  | awk '/Receivers:/{r=1;next} r&&/action:/{a=$0; sub(/^[[:space:]]*action:[[:space:]]*/,"",a); if(a ~ /LATE|SCREEN|USAGE|ROLLUP/) {print a; exit}}'
}
ACTION="${ACTION_OVERRIDE:-$(discover_action)}"
[ -z "$ACTION" ] && ACTION="$PKG.ACTION_RUN_UNLOCK_ROLLUP"

adb exec-out run-as "$PKG" sh -c '
  mkdir -p files
  [ -f files/daily_late_screen.csv ] || printf "date,late_minutes\n" > files/daily_late_screen.csv
' >/dev/null 2>&1

mtime() {
  adb exec-out run-as "$PKG" sh -c "toybox stat -c %Y $F 2>/dev/null || busybox stat -c %Y $F 2>/dev/null || echo 0" | tr -d '\r'
}
today_has_row() {
  local d="$(adb shell 'date +%F' | tr -d '\r')"
  adb exec-out run-as "$PKG" sh -c "cat $F 2>/dev/null" | awk -F, -v d="$d" 'NR>1&&$1==d{f=1} END{print (f?1:0)}'
}

M0="$(mtime)"
adb shell cmd deviceidle force-idle >/dev/null 2>&1 || true
adb shell cmd deviceidle tempwhitelist "$PKG" >/dev/null 2>&1 || true
adb shell am broadcast --receiver-foreground -a "$ACTION" --ei backfill_days 1 -n "$PKG"/.TriggerReceiver >/dev/null 2>&1 || true

ok=0; t=0
while [ $t -lt 30 ]; do
  sleep 1
  M1="$(mtime)"; R="$(today_has_row)"
  if [ "$M1" != "$M0" ] || [ "$R" = "1" ]; then ok=1; break; fi
  t=$((t+1))
done
adb shell cmd deviceidle unforce >/dev/null 2>&1 || true

if [ $ok -eq 1 ]; then
  echo "LNS EE-3 RESULT=PASS"
  exit 0
else
  echo "LNS EE-3 RESULT=FAIL"
  exit 1
fi
