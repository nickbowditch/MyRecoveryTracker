#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/notification_engagement/tc1.5.txt"
CSV="files/daily_notification_engagement.csv"
LOCK="app/locks/daily_notif_engagement.head"
H1="date,feature_schema_version,delivered,opened,open_rate"
H2="date,posted,removed,clicked"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "TC-1 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "TC-1 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

HDR="$(adb exec-out run-as "$PKG" head -n1 "$CSV" 2>/dev/null | tr -d '\r' || true)"
[ -n "$HDR" ] || { echo "TC-1 RESULT=FAIL (missing csv)" | tee "$OUT"; exit 4; }

EXP="$(tr -d '\r' < "$LOCK" 2>/dev/null || true)"
if [ -n "$EXP" ]; then
  [ "$HDR" = "$EXP" ] || { echo "TC-1 RESULT=FAIL (bad header)" | tee "$OUT"; exit 5; }
else
  case "$HDR" in
    "$H1"|"$H2") : ;;
    *) echo "TC-1 RESULT=FAIL (bad header)"; exit 5 ;;
  esac
fi

TODAY="$(adb shell date +%F 2>/dev/null | tr -d '\r')"
YDAY="$(adb shell toybox date -d 'yesterday' +%F 2>/dev/null | tr -d '\r' || adb shell date -d 'yesterday' +%F 2>/dev/null | tr -d '\r' || echo "")"

ROWS="$(adb exec-out run-as "$PKG" tail -n +2 "$CSV" 2>/dev/null | tr -d '\r')"
ctoday=$(printf '%s\n' "$ROWS" | awk -F',' -v d="$TODAY" '$1==d{c++}END{print c+0}')
cyday=0
[ -n "$YDAY" ] && cyday=$(printf '%s\n' "$ROWS" | awk -F',' -v d="$YDAY" '$1==d{c++}END{print c+0}')

ok=0
if [ "$ctoday" -eq 1 ] && [ "$cyday" -eq 1 ]; then ok=1
elif [ "$ctoday" -eq 1 ] && [ "$cyday" -eq 0 ]; then ok=1
elif [ "$ctoday" -eq 0 ] && [ "$cyday" -eq 1 ]; then ok=1
fi

if [ "$ok" -eq 1 ]; then
  echo "TC-1 RESULT=PASS" | tee "$OUT"; exit 0
else
  echo "TC-1 RESULT=FAIL (rows today=$ctoday yesterday=$cyday)" | tee "$OUT"; exit 6
fi
