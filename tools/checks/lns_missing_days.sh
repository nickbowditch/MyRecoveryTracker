#!/usr/bin/env bash
PKG="com.nick.myrecoverytracker"
F="files/daily_late_screen.csv"
E_NOW="$(adb shell 'toybox date +%s' | tr -d '\r')"
MISS=()
for i in $(seq 0 6); do
  E=$(( E_NOW - 86400*i ))
  D="$(adb shell "toybox date -d '@$E' +%F" | tr -d '\r')"
  HIT="$(adb exec-out run-as "$PKG" awk -F, -v dd="$D" 'NR>1&&$1==dd{f=1} END{print (f?1:0)}' "$F" 2>/dev/null)"
  [ "$HIT" = "1" ] || MISS+=("$D")
done
IFS=,; echo "${MISS[*]}"
