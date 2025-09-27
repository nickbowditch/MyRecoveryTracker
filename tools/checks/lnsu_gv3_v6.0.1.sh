#!/bin/sh
set -eu

APP="${APP:-com.nick.myrecoverytracker}"
ACT="com.nick.myrecoverytracker.ACTION_RUN_LNS_ROLLUP"
RCV="com.nick.myrecoverytracker.TriggerReceiver"

OUT="evidence/v6.0/lnsu/gv3.txt"
CONSOLE="evidence/v6.0/lnsu/gv3.console.txt"
mkdir -p "$(dirname "$OUT")"

adb shell cmd package query-receivers -a "$ACT" "$APP" >"$CONSOLE.query" 2>&1 || true
adb shell dumpsys package "$APP" >"$CONSOLE.pkg" 2>&1 || true

pass=1

grep -q "$RCV" "$CONSOLE.query" || pass=0

awk -v rcv="$RCV" '
  /Receivers:/ { inrc=1; next }
  /Services:/ { inrc=0 }
  inrc && index($0,rcv) { hit=1 }
  inrc && hit && /exported=/ {
    if (index($0,"exported=true")) ok=1; else ok=0
    exit
  }
  END { if (!(hit && ok)) exit 1 }
' "$CONSOLE.pkg" || pass=0

grep -q "$ACT" "$CONSOLE.pkg" || pass=0

if [ "$pass" -eq 1 ]; then
  echo "GV3 RESULT=PASS" | tee "$OUT"
  exit 0
fi

echo "GV3 RESULT=FAIL" | tee "$OUT"
exit 1
