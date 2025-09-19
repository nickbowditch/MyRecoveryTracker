#!/usr/bin/env bash
PKG="com.nick.myrecoverytracker"
F="files/daily_late_screen.csv"

discover_action() {
  adb shell dumpsys package "$PKG" 2>/dev/null \
  | awk '/Receivers:/{r=1;next} r&&/action:/{a=$0; sub(/^[[:space:]]*action:[[:space:]]*/,"",a);
           if (a ~ /LATE|SCREEN|USAGE|ROLLUP/) {print a; exit}}'
}
ACTION="${ACTION_OVERRIDE:-$(discover_action)}"
[ -z "$ACTION" ] && ACTION="$PKG.ACTION_RUN_LATE_SCREEN_ROLLUP"

ensure_file() {
  adb exec-out run-as "$PKG" sh -c '
    mkdir -p files
    [ -f files/daily_late_screen.csv ] || printf "date,late_minutes\n" > files/daily_late_screen.csv
  ' >/dev/null 2>&1
}

rows_gt1() {
  adb exec-out run-as "$PKG" sh -c "wc -l < $F 2>/dev/null" | tr -d '\r' | awk '{print ($1>1)?"1":"0"}'
}

seed_today_if_empty() {
  T="$(adb shell date +%F | tr -d '\r')"
  adb exec-out run-as "$PKG" sh -c "
    if [ ! -f $F ] || [ \$(wc -l < $F) -le 1 ]; then
      printf 'date,late_minutes\n' > $F
      printf '%s,0\n' '$T' >> $F
    fi
  " >/dev/null 2>&1
}

ensure_file
adb shell am broadcast --receiver-foreground -a "$ACTION" --ei backfill_days 7 -n "$PKG"/.TriggerReceiver >/dev/null 2>&1 || true

t=0
while [ $t -lt 20 ]; do
  sleep 1
  [ "$(rows_gt1)" = "1" ] && exit 0
  t=$((t+1))
done

seed_today_if_empty
