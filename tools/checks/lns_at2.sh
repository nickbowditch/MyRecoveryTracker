#!/usr/bin/env bash
PKG="com.nick.myrecoverytracker"

discover() {
  adb shell dumpsys package "$PKG" 2>/dev/null \
  | awk '/Receivers:/{r=1;next} r&&/action:/{a=$0; sub(/^[[:space:]]*action:[[:space:]]*/,"",a);
           if (a ~ /LATE|SCREEN|USAGE|ROLLUP/) {print a; exit}}'
}
ACTION="${ACTION_OVERRIDE:-$(discover)}"
[ -z "$ACTION" ] && ACTION="$PKG.ACTION_RUN_LATE_SCREEN_ROLLUP"

FOUND=""
adb shell am broadcast -a "$ACTION" -n "$PKG"/.TriggerReceiver >/dev/null 2>&1
for i in 1 2 3 4 5 6 7 8; do
  L="$(adb exec-out run-as "$PKG" sh -c 'ls -1 files/.lock files/.LOCK files/.lck files/.LCK files/daily_late_screen.csv.lock files/daily_late_screen.csv.tmp 2>/dev/null' | tr -d '\r')"
  [ -n "$L" ] && FOUND="$L"
  sleep 0.5
done

if [ -z "$FOUND" ]; then
  echo "LNS AT-2 RESULT=PASS"
  exit 0
else
  echo "LNS AT-2 RESULT=FAIL"
  printf "%s\n" "$FOUND"
  exit 1
fi
