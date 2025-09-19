#!/bin/sh
PKG=com.nick.myrecoverytracker

adb shell "run-as $PKG /system/bin/sh -s" <<'EOS'
d=$(date +%F)

printf "%-24s | %-6s | %-19s | %-10s | %-6s | %s\n" \
  "Feature" "Today?" "mtime_epoch" "size(bytes)" "lines" "Last row"
printf "%s\n" "------------------------------------------------------------------------------------------------"

row(){
  f="$1"; label="$2"
  if [ -f "$f" ]; then
    sz=$(wc -c <"$f" 2>/dev/null || echo 0)
    ln=$(wc -l <"$f" 2>/dev/null || echo 0)
    mt=$(stat -c %Y "$f" 2>/dev/null || echo -)
    last=$(tail -n 1 "$f" 2>/dev/null || echo "")
    today="no"
    case "$f" in
      files/unlock_log.csv|files/screen_log.csv)
        case "$last" in
          $d*) today="yes" ;;
        esac
        ;;
      *)
        IFS=, set -- $last
        [ "$1" = "$d" ] && today="yes"
        ;;
    esac
    printf "%-24s | %-6s | %-19s | %-10s | %-6s | %s\n" "$label" "$today" "$mt" "$sz" "$ln" "$last"
  else
    printf "%-24s | %-6s | %-19s | %-10s | %-6s | %s\n" "$label" "miss" "-" "-" "-" "(no file: $f)"
  fi
}

row files/unlock_log.csv "Unlock log"
row files/screen_log.csv "Screen log"
EOS
