#!/usr/bin/env bash
PKG="com.nick.myrecoverytracker"
discover() {
  adb shell dumpsys package "$PKG" 2>/dev/null \
  | awk '/Receivers:/{r=1;next} r&&/action:/{a=$0; sub(/^[[:space:]]*action:[[:space:]]*/,"",a);
           if (a ~ /LATE|SCREEN|USAGE|ROLLUP/) {print a; exit}}'
}
ACTION="${ACTION_OVERRIDE:-$(discover)}"
[ -z "$ACTION" ] && ACTION="$PKG.ACTION_RUN_LATE_SCREEN_ROLLUP"
DAYS="${DAYS:-14}"
adb shell am broadcast --receiver-foreground -a "$ACTION" --ei backfill_days "$DAYS" -n "$PKG"/.TriggerReceiver >/dev/null 2>&1
sleep 2
adb shell am broadcast --receiver-foreground -a "$ACTION" --ei backfill_days "$DAYS" -n "$PKG"/.TriggerReceiver >/dev/null 2>&1
echo "LNS backfill requested for last $DAYS day(s)"
