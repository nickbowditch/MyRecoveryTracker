#!/bin/bash
PKG="com.nick.myrecoverytracker"
ACTION="${ACTION_OVERRIDE:-$PKG.ACTION_RUN_LATE_SCREEN_ROLLUP}"
FILE="files/daily_late_screen.csv"

TODAY="$(adb shell 'date +%F' | tr -d '\r')"

count_for_today() {
  adb exec-out run-as "$PKG" sh -c "cat $FILE 2>/dev/null" | awk -F, -v d="$TODAY" 'NR>1&&$1==d{c++} END{print c+0}'
}

C0="$(count_for_today)"
adb shell am broadcast -a "$ACTION" -n "$PKG"/.TriggerReceiver >/dev/null 2>&1
sleep 1
adb shell am broadcast -a "$ACTION" -n "$PKG"/.TriggerReceiver >/dev/null 2>&1
sleep 1
C1="$(count_for_today)"

if [ -n "$C1" ] && [ "$C1" -le 1 ]; then
  echo "LNS EE-4 RESULT=PASS (rows_today=$C1)"
  exit 0
else
  echo "LNS EE-4 RESULT=FAIL (before=$C0 after=$C1)"
  exit 1
fi
