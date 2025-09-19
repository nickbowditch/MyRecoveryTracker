#!/bin/sh
PKG=com.nick.myrecoverytracker
TODAY=$(date +%F)

adb shell 'run-as '$PKG' /system/bin/sh -c "
  [ -x files/.bat_calc.sh ] && files/.bat_calc.sh
  [ -x files/.lux_rollup.sh ] && files/.lux_rollup.sh
  [ -x files/.calls_rollup_types.sh ] && files/.calls_rollup_types.sh
  [ -x files/.missed_call_ratio_rollup.sh ] && files/.missed_call_ratio_rollup.sh
  [ -x files/.sleep_rollup.sh ] && files/.sleep_rollup.sh
  [ -x files/.notif_rollup.sh ] && files/.notif_rollup.sh
  if [ -f files/usage_events.csv ]; then
    d=\$(date +%F); out=files/daily_usage_events.csv; tmp=files/.ue.tmp
    cnt=\$(awk -F, -v d=\"\$d\" \"index(\\\$1,d)==1{c++} END{print c+0}\" files/usage_events.csv)
    [ -f \"\$out\" ] || echo \"date,count\" > \"\$out\"
    awk -F, -v d=\"\$d\" -v c=\"\$cnt\" \"NR==1{h=\\\$0;next} \\\$1!=d{print} END{print h; print d \\\",\" c}\" \"\$out\" > \"\$tmp\" && mv \"\$tmp\" \"\$out\"
  fi
"'

adb exec-out run-as $PKG sh -c '
d='"$TODAY"';
for f in \
  files/daily_battery_drain.csv \
  files/daily_light_exposure.csv \
  files/daily_calls.csv \
  files/daily_missed_call_ratio.csv \
  files/daily_distance_log.csv \
  files/daily_movement_intensity.csv \
  files/daily_messages_sent.csv \
  files/daily_messages_received.csv \
  files/daily_app_usage_minutes.csv \
  files/daily_usage_events.csv \
  files/daily_late_night_screen_usage.csv \
  files/daily_notification_engagement.csv \
  files/daily_notification_latency.csv \
  files/daily_app_switching.csv \
  files/daily_sleep.csv \
  files/daily_headphone_minutes.csv
do
  [ -f "$f" ] || continue
  awk -F, -v d="$d" "NR==1{next} index(\$1,d)==1{print \"$f:\",\$0}" "$f"
done
'
date '+FINISH %F %T'
