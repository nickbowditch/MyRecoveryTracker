#!/bin/sh
set -e

PKG=com.nick.myrecoverytracker
YEST=$(date -d "yesterday" +%F 2>/dev/null || date -v-1d +%F)

adb shell run-as "$PKG" /system/bin/sh -s "$YEST" <<'EOS'
d="$1"

printf "%-32s | %-9s | %-12s | %-7s | %s\n" \
  "Feature" "yesterday?" "size(bytes)" "lines" "last row"
printf "%s\n" "------------------------------------------------------------------------------------------------"

report() {
  label="$1"; f="$2"
  if [ -f "$f" ]; then
    sz=$(wc -c <"$f" 2>/dev/null || echo 0)
    ln=$(wc -l <"$f" 2>/dev/null || echo 0)
    ycnt=$(grep -c "^$d" "$f" 2>/dev/null || echo 0)
    last=$(tail -n 1 "$f" 2>/dev/null || echo "")
    yn="no"; [ "$ycnt" -gt 0 ] && yn="yes"
    printf "%-32s | %-9s | %-12s | %-7s | %s\n" "$label" "$yn" "$sz" "$ln" "$last"
    tail -n 2 "$f" 2>/dev/null || true
    echo ""
  else
    printf "%-32s | %-9s | %-12s | %-7s | %s\n" "$label" "missing" "-" "-" "(no file: $f)"
    echo ""
  fi
}

report "Late night screen daily" "files/daily_late_night_screen_usage.csv"
report "Sleep daily"            "files/daily_sleep.csv"
EOS
