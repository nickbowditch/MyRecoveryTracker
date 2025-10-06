#!/bin/bash
set -euo pipefail
PKG="com.nick.myrecoverytracker"
RCV="$PKG/.TriggerReceiver"
CSV="files/daily_movement_intensity.csv"
OUT="evidence/v6.0/movement_intensity/at1.txt"

mkdir -p "$(dirname "$OUT")"
exec > >(tee "$OUT") 2>&1
fail(){ echo "AT1 RESULT=FAIL ($1)"; exit 1; }

hash_stream(){ if command -v sha1sum >/dev/null 2>&1; then sha1sum | awk '{print $1}'; else shasum -a 1 | awk '{print $1}'; fi; }

adb get-state >/dev/null 2>&1 || { echo "AT1 RESULT=FAIL (no device)"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "AT1 RESULT=FAIL (app not installed)"; exit 3; }

HDR="$(adb exec-out run-as "$PKG" sed -n '1p' "$CSV" 2>/dev/null | tr -d '\r' || true)"
[ "$HDR" = "date,intensity" ] || fail "CSV header mismatch or missing"

TODAY="$(adb shell 'toybox date +%F 2>/dev/null || date +%F' | tr -d $'\r')"
[ -n "$TODAY" ] || fail "date read error"

PRE_HASH="$(adb exec-out run-as "$PKG" cat "$CSV" 2>/dev/null | tr -d '\r' | hash_stream || true)"
PRE_ROW="$(adb exec-out run-as "$PKG" awk -F, -v d="$TODAY" 'NR>1 && $1==d{print $0; exit}' "$CSV" 2>/dev/null | tr -d '\r' || true)"
echo "pre_hash=${PRE_HASH:-<none>}"
echo "pre_row_today=${PRE_ROW:-<none>}"

adb logcat -c >/dev/null 2>&1 || true
adb shell am broadcast -n "$RCV" -a "$PKG.ACTION_RUN_MOVEMENT_INTENSITY" >/dev/null 2>&1 || true

adb shell dumpsys jobscheduler \
| sed -n 's/.#u[0-9a-zA-Z]\+\/\([0-9]\+\)./\1/p' \
| while read -r JID; do
adb shell cmd jobscheduler run -f "$PKG" "$JID" >/dev/null 2>&1 || true
done

deadline=$((SECONDS+25))
POST_HASH=""
POST_ROW=""
CHANGED=0
while (( SECONDS < deadline )); do
POST_HASH="$(adb exec-out run-as "$PKG" cat "$CSV" 2>/dev/null | tr -d '\r' | hash_stream || true)"
POST_ROW="$(adb exec-out run-as "$PKG" awk -F, -v d="$TODAY" 'NR>1 && $1==d{print $0; exit}' "$CSV" 2>/dev/null | tr -d '\r' || true)"
if [ -n "$POST_HASH" ] && [ "$POST_HASH" != "$PRE_HASH" ]; then CHANGED=1; break; fi
if [ -n "$POST_ROW" ] && [ "$POST_ROW" != "$PRE_ROW" ]; then CHANGED=1; break; fi
sleep 1
done

echo "post_hash=${POST_HASH:-<none>}"
echo "post_row_today=${POST_ROW:-<none>}"
echo "--- DEBUG: CSV head ---"
adb exec-out run-as "$PKG" sh -c 'head -n 10 "'"$CSV"'" 2>/dev/null | tr -d "\r"' || true
echo "--- DEBUG: CSV tail ---"
adb exec-out run-as "$PKG" sh -c 'tail -n 10 "'"$CSV"'" 2>/dev/null | tr -d "\r"' || true
echo "--- DEBUG: WorkManager success logs ---"
adb logcat -d -v brief 2>/dev/null | grep -E 'MovementIntensity|IntensityWorker|WorkManager' | grep -Ei 'SUCCEEDED|Succeeded|Finished|Completed' | tail -n 20 || true

[ "$CHANGED" -eq 1 ] || fail "CSV did not change after manual trigger"
echo "AT1 RESULT=PASS"
exit 0
