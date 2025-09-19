#!/bin/sh
set -e
PKG=com.nick.myrecoverytracker
adb shell am start -n $PKG/.MainActivity >/dev/null 2>&1 || true
sleep 2
PID="$(adb shell pidof $PKG 2>/dev/null | tr -d '\r')"
echo "PID=$PID"
adb shell dumpsys activity broadcasts | sed -n '/Registered Receivers:/,/Receiver Resolver Table:/p' | awk -v pkg="$PKG" 'p||/Registered Receivers:/ {p=1} /Receiver Resolver Table:/ {exit} /ReceiverList/ && index($0,pkg) {show=1} show{print} /ReceiverList/ && !index($0,pkg){show=0}' | egrep -i 'ReceiverList|ScreenReceiver|SCREEN_ON|SCREEN_OFF' || echo "(none found)"
adb logcat -c
adb shell input keyevent 26
sleep 2
adb shell input keyevent 26
sleep 2
adb shell input keyevent 82
sleep 1
adb exec-out run-as $PKG tail -n 10 files/screen_log.csv 2>/dev/null || echo "(no file)"
adb logcat -d | egrep -i 'com\.nick\.myrecoverytracker|ScreenReceiver|MainApplication' -n || echo "(no app lines in logcat)"
adb shell 'run-as '"$PKG"' /system/bin/sh -c "
  f=files/screen_log.csv
  if [ -f \$f ]; then
    echo -n mtime_epoch=; stat -c %Y \$f 2>/dev/null || echo -
    echo -n size_bytes=; wc -c < \$f 2>/dev/null || echo 0
    echo -n line_count=; wc -l < \$f 2>/dev/null || echo 0
  else
    echo \"(no file)\"
  fi
"'
