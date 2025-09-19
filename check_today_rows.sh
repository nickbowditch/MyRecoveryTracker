#!/bin/sh
PKG=com.nick.myrecoverytracker

adb shell "run-as $PKG /system/bin/sh -s" <<'EOS'
d=$(date +%F)

printf "%-34s | %-6s | %-19s | %-12s | %-8s | %s\n" \
  "Feature" "Today?" "mtime_epoch" "size(bytes)" "lines" "Last row"
printf "%s\n" "-------------------------------------------------------------------------------------------------------------"

row(){
  f="$1"
  if [ -f "$f" ]; then
    sz=$(wc -c <"$f" 2>/dev/null || echo 0)
    ln=$(wc -l <"$f" 2>/dev/null || echo 0)
    mt=$(stat -c %Y "$f" 2>/dev/null || echo -)
    last=$(tail -n 1 "$f" 2>/dev/null || echo "")
    today="no"
    case "$f" in
      files/notification_log.csv|files/unlock_log.csv|files/screen_log.csv)
        case "$last" in
          $d*) today="yes" ;;
        esac
        ;;
      *)
        IFS=, set -- $last
        [ "$1" = "$d" ] && today="yes"
        ;;
    esac
    printf "%-34s | %-6s | %-19s | %-12s | %-8s | %s\n" "$2" "$today" "$mt" "$sz" "$ln" "$last"
  else
    printf "%-34s | %-6s | %-19s | %-12s | %-8s | %s\n" "$2" "miss" "-" "-" "-" "(no file: $f)"
  fi
}

row files/unlock_log.csv                        "Unlock log"
row files/screen_log.csv                        "Screen log"
row files/notification_log.csv                  "Notification log (raw)"
row files/daily_notification_engagement.csv     "Notification engagement daily"
row files/daily_notification_latency.csv        "Notification latency daily"
EOS
