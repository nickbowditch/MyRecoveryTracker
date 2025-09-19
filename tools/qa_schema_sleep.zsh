#!/usr/bin/env zsh
PKG="${1:-com.nick.myrecoverytracker}"
S=0
exp_header () { jq -r ".$1.header | @csv" schemas/rollups.json | sed 's/^"//; s/"$//; s/","/,/g'; }
pull_header () { adb exec-out run-as "$PKG" head -n1 "files/$1" 2>/dev/null || echo "__MISSING__"; }
chk () {
  local key="$1" file="$(jq -r ".$1.file // empty" schemas/rollups.json)"
  [ -z "$file" ] && file="$1"
  local exp="$(exp_header "$key")"
  local got="$(pull_header "$file")"
  if [ "$got" != "$exp" ]; then
    echo "FAIL: $file header drift (got='$got' exp='$exp')"; S=1
  fi
}
chk "sleep_summary"
chk "sleep_duration"
chk "sleep_quality"
chk "sleep_time"
chk "sleep_wake_time"
[ $S -eq 0 ] && echo "SCHEMA: OK" || echo "SCHEMA: FAIL"
exit $S
