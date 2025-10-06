#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
CSV="files/daily_app_switching.csv"
LOCK="app/locks/daily_app_switching.header"
RCV="$PKG/.TriggerReceiver"
ACT="$PKG.ACTION_RUN_APP_SWITCHING_DAILY"
OUT="evidence/v6.0/app_switching/gv7.1.txt"
mkdir -p "$(dirname "$OUT")" app/locks

adb get-state >/dev/null 2>&1 || { echo "GV7 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "GV7 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

T="$(adb shell toybox date +%F | tr -d '\r')"

adb shell run-as "$PKG" sh <<IN
set -eu
csv="$CSV"
hdr="date,switches,entropy"
mkdir -p files
echo "\$hdr" > "\$csv"
IN
printf "date,switches,entropy" > "$LOCK"

adb logcat -c >/dev/null 2>&1 || true
adb shell cmd activity broadcast -n "$RCV" -a "$ACT" --receiver-foreground --user 0 >/dev/null 2>&1 || true

deadline=$(( $(date +%s) + 25 ))
hdr=""
row=""
while :; do
hdr="$(adb exec-out run-as "$PKG" sed -n '1p' "$CSV" 2>/dev/null | tr -d '\r' || true)"
row="$(adb exec-out run-as "$PKG" awk -F, -v d="$T" 'NR>1 && $1==d{print;exit}' "$CSV" 2>/dev/null | tr -d '\r' || true)"
[ -n "$row" ] && break
[ "$(date +%s)" -ge "$deadline" ] && break
sleep 1
done

[ "$hdr" = "date,switches,entropy" ] || {
{
echo "GV7 RESULT=FAIL (bad header)"
echo "--- EXPECTED ---"
echo "date,switches,entropy"
echo "--- ACTUAL ---"
echo "${hdr:-[missing]}"
} | tee "$OUT"
exit 1
}

[ -n "$row" ] || {
{
echo "GV7 RESULT=FAIL (missing row for $T)"
echo "--- HEAD ---"
adb exec-out run-as "$PKG" head -n5 "$CSV" 2>/dev/null | tr -d '\r' || echo "[no csv]"
echo "--- LOGCAT ---"
adb logcat -d 2>/dev/null | grep -iE 'TriggerReceiver|AppSwitchingDaily|WorkManager' | tail -n 80 || echo "[no logs]"
} | tee "$OUT"
exit 1
}

S="$(printf '%s\n' "$row" | awk -F, '{print $2}')"
E="$(printf '%s\n' "$row" | awk -F, '{print $3}')"

echo "$S" | grep -Eq '^[0-9]+$' || { echo "GV7 RESULT=FAIL (non-integer switches: $S)" | tee "$OUT"; exit 1; }
awk -v s="$S" 'BEGIN{exit (s>=0 && s<=10000)?0:1}' || { echo "GV7 RESULT=FAIL (switches out of range: $S)" | tee "$OUT"; exit 1; }

echo "$E" | grep -Eq '^[0-9]+([.][0-9]+)?$' || { echo "GV7 RESULT=FAIL (non-numeric entropy: $E)" | tee "$OUT"; exit 1; }
awk -v e="$E" 'BEGIN{exit (e>=0)?0:1}' || { echo "GV7 RESULT=FAIL (negative entropy: $E)" | tee "$OUT"; exit 1; }

echo "GV7 RESULT=PASS" | tee "$OUT"
exit 0
