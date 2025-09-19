#!/usr/bin/env bash
PKG="com.nick.myrecoverytracker"
TODAY="$(adb shell 'date +%F' | tr -d '\r')"
CSV="$(adb exec-out run-as "$PKG" cat files/daily_sleep_summary.csv 2>/dev/null || printf "")"
BAD="$(awk -F, -v t="$TODAY" 'NR>1 && $1>t' <<<"$CSV")"
[ -z "$BAD" ] && echo "Sleep TC-3 RESULT=PASS" || { echo "Sleep TC-3 RESULT=FAIL"; printf "%s\n" "$BAD"; exit 1; }
