#!/bin/bash
PKG="com.nick.myrecoverytracker"
D_LOCAL="$(adb shell 'date +%F' | tr -d '\r')"
D_UTC="$(adb shell 'TZ=UTC date +%F' | tr -d '\r')"
CSV="$(adb exec-out run-as "$PKG" cat files/daily_unlocks.csv 2>/dev/null || printf "")"
CL="$(awk -F, -v d="$D_LOCAL" 'NR>1&&$1==d{c++} END{print c+0}' <<<"$CSV")"
CU="$(awk -F, -v d="$D_UTC"   'NR>1&&$1==d{c++} END{print c+0}' <<<"$CSV")"
test "$CL" -eq 1 -a "$CU" -eq 0
