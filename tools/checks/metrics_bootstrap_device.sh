#!/usr/bin/env bash
set -euo pipefail
PKG="com.nick.myrecoverytracker"
BASE="date,feature_schema_version,daily_unlocks"
HDR="${BASE}${EXTRA:+,$EXTRA}"
adb exec-out run-as "$PKG" sh -c '
  mkdir -p files
  : > files/daily_metrics.csv
'
adb exec-out run-as "$PKG" sh -c "printf '%s\n' \"$HDR\" > files/daily_metrics.csv"
adb exec-out run-as "$PKG" head -n1 files/daily_metrics.csv | tr -d '\r'
