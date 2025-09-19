#!/bin/bash
PKG="com.nick.myrecoverytracker"
adb shell dumpsys package "$PKG" 2>/dev/null | awk '
/Receivers:/ {r=1; next} r&&/E: /{next} r&&/^[ ]*Receiver/{next}
r&&/action:|android.intent.action/{
  gsub(/^[ \t]*/,""); gsub(/^action:/,"");
  if($0 ~ /RUN|LATE|SCREEN|USAGE|ROLLUP/) print
}'
