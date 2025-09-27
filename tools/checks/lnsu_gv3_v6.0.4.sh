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
if ($0 ~ ("name=" rcv)) {
inrc=1
has_export=0
has_action=0
next
}
if (inrc && $0 ~ /name=/) {
if (!(has_export && has_action)) exit 1
inrc=0
}
if (inrc && /exported=/) {
if ($0 ~ /exported=true/) has_export=1
}
if (inrc && index($0, act)) {
has_action=1
}
}
END {
if (inrc && !(has_export && has_action)) exit 1
}
' "$CON_PKG" || pass=0

if [ "$pass" -eq 1 ]; then
echo "GV3 RESULT=PASS" | tee "$OUT"
exit 0
fi

echo "GV3 RESULT=FAIL" | tee "$OUT"
exit 1
