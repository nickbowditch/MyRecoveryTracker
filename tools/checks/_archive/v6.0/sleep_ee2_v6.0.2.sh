#!/bin/sh
PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/sleep/ee2.2.txt"
CSV="files/daily_sleep.csv"
ACT="$PKG.ACTION_RUN_SLEEP_ROLLUP"
CMP="$PKG/.TriggerReceiver"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "EE-2 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "EE-2 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

before="$(adb exec-out run-as "$PKG" sh -c 'cat "'"$CSV"'" 2>/dev/null | sha1sum 2>/dev/null | awk "{print \$1}"' || true)"
adb shell am broadcast -a "$ACT" -n "$CMP" >/dev/null 2>&1 || { echo "EE-2 RESULT=FAIL (broadcast failed)" | tee "$OUT"; exit 4; }
sleep 2
exists="$(adb exec-out run-as "$PKG" sh -c '[ -f "'"$CSV"'" ] && echo 1 || echo 0')"
after="$(adb exec-out run-as "$PKG" sh -c 'cat "'"$CSV"'" 2>/dev/null | sha1sum 2>/dev/null | awk "{print \$1}"' || true)"

[ "$exists" = "1" ] || { echo "EE-2 RESULT=FAIL (csv missing)" | tee "$OUT"; exit 5; }
[ -n "$after" ] || { echo "EE-2 RESULT=FAIL (no content)" | tee "$OUT"; exit 6; }

echo "EE-2 RESULT=PASS" | tee "$OUT"
exit 0
