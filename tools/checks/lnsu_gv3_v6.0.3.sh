#!/bin/sh
set -eu

APP="${APP:-com.nick.myrecoverytracker}"
ACT="com.nick.myrecoverytracker.ACTION_RUN_LNS_ROLLUP"
RCV_FQ="com.nick.myrecoverytracker.TriggerReceiver"

OUT="evidence/v6.0/lnsu/gv3.txt"
CONSOLE_DIR="evidence/v6.0/lnsu"
CON_QUERY="$CONSOLE_DIR/gv3.query.txt"
CON_PKG="$CONSOLE_DIR/gv3.pkg.txt"
mkdir -p "$CONSOLE_DIR"

adb shell cmd package query-receivers -a "$ACT" "$APP" >"$CON_QUERY" 2>&1 || true
adb shell dumpsys package "$APP" >"$CON_PKG" 2>&1 || true

pass=1

grep -q "$RCV_FQ" "$CON_QUERY" || pass=0

awk -v act="$ACT" -v rcv="$RCV_FQ" '
  /Receivers:/ { insec=1; next }
  /Services:/  { insec=0 }
  insec {
    if ($0 ~ "name=" rcv) {
      inrc=1
      exp=0
      actok=0
    } else if (inrc && $0 ~ /name=/) {
      done=1
    }
    if (inrc && $0 ~ /exported=/) {
      if ($0 ~ /exported=true/) exp=1
    }
    if (inrc && index($0, act)) {
      actok=1
    }
    if (done) {
      if (!(exp && actok)) exit 1
      inrc=0
      done=0
    }
  }
  END {
    if (inrc && !(exp && actok)) exit 1
  }
' "$CON_PKG" || pass=0

if [ "$pass" -eq 1 ]; then
  echo "GV3 RESULT=PASS" | tee "$OUT"
  exit 0
fi

echo "GV3 RESULT=FAIL" | tee "$OUT"
exit 1
