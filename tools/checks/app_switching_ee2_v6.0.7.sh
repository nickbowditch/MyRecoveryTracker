#!/bin/sh
set -eu

PKG="com.nick.myrecoverytracker"
RCV="$PKG/.TriggerReceiver"
ACT="$PKG.ACTION_RUN_APP_SWITCHING_DAILY"
CSV_OUT="files/daily_app_switching.csv"
CSV_IN="files/usage_events.csv"
OUT="evidence/v6.0/app_switching/ee2.txt"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "EE-2 RESULT=FAIL (no device/emulator)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "EE-2 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

T="$(adb shell toybox date +%F 2>/dev/null | tr -d '\r')"

adb shell run-as "$PKG" sh <<IN
set -eu
mkdir -p files
f="$CSV_IN"
echo "date,time,event_type,package" > "\$f"
echo "$T,10:00:00,FOREGROUND,com.test.a" >> "\$f"
echo "$T,10:05:00,FOREGROUND,com.test.b" >> "\$f"
echo "$T,10:10:00,FOREGROUND,com.test.a" >> "\$f"
IN

adb shell run-as "$PKG" sh -c '
set -eu
f="'"$CSV_OUT"'"
tmp="$f.tmp"
if [ -f "$f" ]; then
{ head -n1 "$f" 2>/dev/null | grep -q "^date,switches,entropy$" || echo "date,switches,entropy"; tail -n +2 "$f" 2>/dev/null | awk -F, -v d="'"$T"'" "!(\$1==d)"; } > "$tmp" || echo "date,switches,entropy" > "$tmp"
mv "$tmp" "$f"
else
echo "date,switches,entropy" > "$f"
fi
' >/dev/null 2>&1 || true

adb logcat -c >/dev/null 2>&1 || true
adb shell cmd activity broadcast -n "$RCV" -a "$ACT" --receiver-foreground --user 0 >/dev/null 2>&1 || true

deadline=$(( $(date +%s) + 35 ))
HDR=""
ROW_T=""
while :; do
  HDR="$(adb exec-out run-as "$PKG" sed -n '1p' "$CSV_OUT" 2>/dev/null | tr -d '\r' || true)"
  ROW_T="$(adb exec-out run-as "$PKG" awk -F, -v d="$T" 'NR>1&&$1==d{print;exit}' "$CSV_OUT" 2>/dev/null | tr -d '\r' || true)"
  [ -n "$ROW_T" ] && break
  [ "$(date +%s)" -ge "$deadline" ] && break
  sleep 1
done

{
echo "=== HEADER ==="
echo "${HDR:-[missing]}"
echo
echo "=== TODAY ($T) ==="
[ -n "$ROW_T" ] && echo "$ROW_T" || echo "[none]"
} | tee "$OUT" >/dev/null

[ "$HDR" = "date,switches,entropy" ] || { echo "EE-2 RESULT=FAIL" | tee -a "$OUT"; exit 1; }
[ -n "$ROW_T" ] || { echo "EE-2 RESULT=FAIL" | tee -a "$OUT"; exit 1; }

SW="$(printf '%s\n' "$ROW_T" | awk -F, '{print $2}')"
EN="$(printf '%s\n' "$ROW_T" | awk -F, '{print $3}')"

printf '%s\n' "$SW" | grep -Eq '^[0-9]+$' || { echo "EE-2 RESULT=FAIL" | tee -a "$OUT"; exit 1; }
printf '%s\n' "$EN" | grep -Eq '^[0-9]+([.][0-9]+)?$' || { echo "EE-2 RESULT=FAIL" | tee -a "$OUT"; exit 1; }

echo "EE-2 RESULT=PASS" | tee -a "$OUT"
exit 0
