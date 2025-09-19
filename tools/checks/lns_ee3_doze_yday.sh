#!/usr/bin/env bash
PKG="com.nick.myrecoverytracker"
F="files/daily_late_screen.csv"

discover_action() {
  adb shell dumpsys package "$PKG" 2>/dev/null \
  | awk '/Receivers:/{r=1;next} r&&/action:/{a=$0; sub(/^[[:space:]]*action:[[:space:]]*/,"",a);
           if (a ~ /LATE|SCREEN|USAGE|ROLLUP/) {print a; exit}}'
}
ACTION="${ACTION_OVERRIDE:-$(discover_action)}"
[ -z "$ACTION" ] && ACTION="com.nick.myrecoverytracker.ACTION_RUN_LATE_SCREEN_ROLLUP"

adb exec-out run-as "$PKG" sh -c '
  mkdir -p files
  [ -f files/daily_late_screen.csv ] || printf "date,late_minutes\n" > files/daily_late_screen.csv
' >/dev/null 2>&1

E_NOW="$(adb shell "toybox date +%s" | tr -d '\r')"
E_YDAY=$(( E_NOW - 86400 ))
YDAY="$(adb shell "toybox date -d '@$E_YDAY' +%F" | tr -d '\r')"

has_row() {
  adb exec-out run-as "$PKG" sh -c "cat $F 2>/dev/null" \
  | awk -F, -v d="$YDAY" 'NR>1&&$1==d{f=1} END{print (f?1:0)}'
}

adb shell cmd deviceidle force-idle >/dev/null 2>&1 || true
adb shell cmd deviceidle tempwhitelist "$PKG" >/dev/null 2>&1 || true

adb shell am broadcast --receiver-foreground -a "$ACTION" --ei backfill_days 2 -n "$PKG"/.TriggerReceiver >/dev/null 2>&1 || true

ok=0; t=0
while [ $t -lt 60 ]; do
  sleep 2
  [ "$(has_row)" = "1" ] && { ok=1; break; }
  t=$((t+2))
done

adb shell cmd deviceidle unforce >/dev/null 2>&1 || true

[ $ok -eq 1 ] && echo "LNS EE-3 RESULT=PASS (yesterday row present)" || { echo "LNS EE-3 RESULT=FAIL (no yesterday row)"; exit 1; }
