#!/bin/bash
PKG="${PKG:-com.nick.myrecoverytracker}"
F="files/daily_metrics.csv"
LOCK="app/locks/daily_metrics.header"
HDR="$(adb exec-out run-as "$PKG" head -n1 "$F" 2>/dev/null | tr -d $'\r')"
[ -n "$HDR" ] || { echo "FAIL: cannot read header from $F"; exit 1; }
mkdir -p "$(dirname "$LOCK")"
printf "%s" "$HDR" > "$LOCK"
echo "Sealed daily_metrics header to $LOCK"
