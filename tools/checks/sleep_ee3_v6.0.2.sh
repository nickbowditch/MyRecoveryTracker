#!/bin/sh
PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/sleep/ee3.2.txt"
CSV="files/daily_sleep_summary.csv"
ACT="$PKG.ACTION_RUN_SLEEP_ROLLUP"
CMP="$PKG/.TriggerReceiver"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "EE-3 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "EE-3 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

h0="$(adb exec-out run-as "$PKG" sh -c 'tr -d "\r" < "'"$CSV"'" 2>/dev/null | sha1sum 2>/dev/null | awk "{print \$1}"' || true)"
adb shell am broadcast -a "$ACT" -n "$CMP" >/dev/null 2>&1 || { echo "EE-3 RESULT=FAIL (broadcast1 failed)" | tee "$OUT"; exit 4; }
sleep 2
h1="$(adb exec-out run-as "$PKG" sh -c 'tr -d "\r" < "'"$CSV"'" 2>/dev/null | sha1sum 2>/dev/null | awk "{print \$1}"' || true)"
adb shell am broadcast -a "$ACT" -n "$CMP" >/dev/null 2>&1 || { echo "EE-3 RESULT=FAIL (broadcast2 failed)" | tee "$OUT"; exit 5; }
sleep 2
h2="$(adb exec-out run-as "$PKG" sh -c 'tr -d "\r" < "'"$CSV"'" 2>/dev/null | sha1sum 2>/dev/null | awk "{print \$1}"' || true)"

[ -n "$h1" ] || { echo "EE-3 RESULT=FAIL (no content after run)" | tee "$OUT"; exit 6; }
[ "$h1" = "$h2" ] || { echo "EE-3 RESULT=FAIL (non-idempotent)" | tee "$OUT"; exit 7; }

echo "EE-3 RESULT=PASS" | tee "$OUT"
exit 0
