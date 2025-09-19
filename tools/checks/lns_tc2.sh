#!/usr/bin/env bash
PKG="com.nick.myrecoverytracker"

CSV="$(adb exec-out run-as "$PKG" cat files/daily_late_screen.csv 2>/dev/null || printf "")"
TODAY="$(adb shell 'toybox date +%F' | tr -d '\r')"
LATEST="$(printf "%s\n" "$CSV" | awk -F, 'NR>1{d=$1} END{print d}')"
[ -z "$LATEST" ] && { echo "LNS TC-2 RESULT=SKIP (no data)"; exit 0; }

T_EPOCH="$(adb shell "toybox date -d '$TODAY 12:00:00' +%s" | tr -d '\r')"
L_EPOCH="$(adb shell "toybox date -d '$LATEST 12:00:00' +%s" | tr -d '\r')"
[ -z "$T_EPOCH" ] || [ -z "$L_EPOCH" ] && { echo "LNS TC-2 RESULT=FAIL (date parse error)"; exit 1; }

DEL_DAYS=$(( (T_EPOCH - L_EPOCH) / 86400 ))
[ "$DEL_DAYS" -lt 0 ] && DEL_DAYS=$(( -DEL_DAYS ))

if [ "$DEL_DAYS" -le 1 ]; then
  echo "LNS TC-2 RESULT=PASS (latest=$LATEST today=$TODAY)"
  exit 0
else
  echo "LNS TC-2 RESULT=FAIL (latest=$LATEST today=$TODAY)"
  exit 1
fi
