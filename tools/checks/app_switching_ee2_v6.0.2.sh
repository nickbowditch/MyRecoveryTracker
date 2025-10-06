#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
RCV="$PKG/.TriggerReceiver"
ACT_SWITCH="$PKG.ACTION_RUN_APP_SWITCHING_DAILY"
ACT_USAGE="$PKG.ACTION_RUN_USAGE_CAPTURE"
CSV_SWITCH="files/daily_app_switching.csv"
CSV_EVENTS="files/usage_events.csv"
OUT="evidence/v6.0/app_switching/ee2.txt"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "EE-2 RESULT=FAIL (no device/emulator)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "EE-2 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

SRC_TRIG_DECL="$(grep -R --include='*.kt' -n 'ACTION_RUN_APP_SWITCHING_DAILY' app/src/main/java 2>/dev/null || true)"
SRC_TRIG_CASE="$(grep -R --include='*.kt' -nE 'when *\(action\)|ACTION_RUN_APP_SWITCHING_DAILY' app/src/main/java 2>/dev/null | grep -n 'ACTION_RUN_APP_SWITCHING_DAILY' || true)"
SRC_MANIF="$(grep -n "$ACT_SWITCH" app/src/main/AndroidManifest.xml 2>/dev/null || true)"

if [ -z "$SRC_TRIG_DECL" ] || [ -z "$SRC_MANIF" ]; then
{
echo "ACTION_SWITCH_DECL=${SRC_TRIG_DECL:+YES}"
echo "ACTION_SWITCH_IN_MANIFEST=${SRC_MANIF:+YES}"
echo "EE-2 RESULT=FAIL (switch action not wired)"
} | tee "$OUT"
exit 1
fi

adb logcat -c >/dev/null 2>&1 || true
adb shell run-as "$PKG" rm -f "$CSV_SWITCH" >/dev/null 2>&1 || true

adb shell cmd activity broadcast -n "$RCV" -a "$ACT_USAGE" --receiver-foreground --user 0 >/dev/null 2>&1 || true
sleep 2

EV_HDR="$(adb exec-out run-as "$PKG" sed -n '1p' "$CSV_EVENTS" 2>/dev/null | tr -d '\r' || true)"
[ "$EV_HDR" = "date,time,event_type,package" ] || EV_HDR="${EV_HDR:-[missing]}"

adb shell cmd activity broadcast -n "$RCV" -a "$ACT_SWITCH" --receiver-foreground --user 0 >/dev/null 2>&1 || true

i=0; HDR=""
while [ $i -lt 20 ]; do
HDR="$(adb exec-out run-as "$PKG" sed -n '1p' "$CSV_SWITCH" 2>/dev/null | tr -d '\r' || true)"
[ -n "$HDR" ] && break
sleep 1
i=$((i+1))
done

ROWS="$(adb exec-out run-as "$PKG" head -n5 "$CSV_SWITCH" 2>/dev/null | tr -d '\r' || true)"
LOGS="$(adb logcat -d 2>/dev/null | grep -iE 'AppSwitching|TriggerReceiver|WorkManager|enqueue|worker' | tail -n 60 || true)"

{
echo "EVENTS_HEADER=$EV_HDR"
echo "SWITCH_HEADER=${HDR:-[missing]}"
echo "--- SWITCH_FIRST5 ---"
[ -n "$ROWS" ] && echo "$ROWS" || echo "[none]"
echo "--- LOGCAT_TAIL ---"
[ -n "$LOGS" ] && echo "$LOGS" || echo "[none]"
} | tee "$OUT" >/dev/null

[ "$HDR" = "date,switches,entropy" ] && { echo "EE-2 RESULT=PASS" | tee -a "$OUT"; exit 0; }
echo "EE-2 RESULT=FAIL" | tee -a "$OUT"; exit 1
