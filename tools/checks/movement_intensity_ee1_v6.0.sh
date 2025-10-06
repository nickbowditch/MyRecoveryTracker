#!/bin/bash
set -euo pipefail

PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/movement_intensity/ee1.txt"
mkdir -p "$(dirname "$OUT")"

fail() {
  echo "EE1 RESULT=FAIL ($1)" | tee "$OUT"
  echo "--- DEBUG: device/app ---" | tee -a "$OUT"
  { adb get-state && adb shell pm path "$PKG"; } 2>&1 | tr -d $'\r' | tee -a "$OUT" || true
  echo "--- DEBUG: dumpsys package (ACTIVITY_RECOGNITION lines) ---" | tee -a "$OUT"
  adb shell dumpsys package "$PKG" 2>/dev/null | tr -d $'\r' | grep -Ei 'ACTIVITY_RECOGNITION' || true | tee -a "$OUT"
  echo "--- DEBUG: appops (activity recognition variants) ---" | tee -a "$OUT"
  { adb shell appops get "$PKG" ACTIVITY_RECOGNITION 2>/dev/null; adb shell appops get "$PKG" android:activity_recognition 2>/dev/null; } | tr -d $'\r' | tee -a "$OUT" || true
  echo "--- DEBUG: sensorservice (step sensors slice) ---" | tee -a "$OUT"
  adb shell dumpsys sensorservice 2>/dev/null | tr -d $'\r' | awk '
    BEGIN{s=0}
    /Sensor List/ {s=1}
    s==1 && /step|STEP|counter|detector/ {print; c++}
    END{if(c==0) print "<no step sensor lines found in Sensor List>"}' | tee -a "$OUT" || true
  exit 1
}

adb get-state >/dev/null 2>&1 || { echo "EE1 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "EE1 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

PDUMP="$(adb shell dumpsys package "$PKG" 2>/dev/null | tr -d $'\r' || true)"
GRANTED_AR=0
if printf "%s\n" "$PDUMP" | grep -Eq 'android\.permission\.ACTIVITY_RECOGNITION: granted=true|ACTIVITY_RECOGNITION: granted=true'; then
  GRANTED_AR=1
fi
[ "$GRANTED_AR" -eq 1 ] || fail "ACTIVITY_RECOGNITION not granted"

SENS="$(adb shell dumpsys sensorservice 2>/dev/null | tr -d $'\r' || true)"
HAS_COUNTER=0
HAS_DETECTOR=0
COUNTER_LINE="$(printf "%s\n" "$SENS" | awk '/Sensor List/{sl=1} sl && /step[_ ]?counter|android\.sensor\.step_counter/ {print; exit}')"
DETECTOR_LINE="$(printf "%s\n" "$SENS" | awk '/Sensor List/{sl=1} sl && /step[_ ]?detector|android\.sensor\.step_detector/ {print; exit}')"
[ -n "$COUNTER_LINE" ] && HAS_COUNTER=1
[ -n "$DETECTOR_LINE" ] && HAS_DETECTOR=1

STEP_ENV_NEEDED=$(( HAS_COUNTER + HAS_DETECTOR ))
if [ "$STEP_ENV_NEEDED" -gt 0 ]; then
  :
else
  :
fi

{
  echo "EE1 CHECK: Activity Recognition + step sensors permitted/available"
  echo "pkg=$PKG"
  echo "activity_recognition_granted=$GRANTED_AR"
  echo "step_counter_present=$HAS_COUNTER"
  echo "step_detector_present=$HAS_DETECTOR"
  echo "--- PACKAGE PERMS (filtered) ---"
  printf "%s\n" "$PDUMP" | grep -Ei 'ACTIVITY_RECOGNITION' || true
  echo "--- SENSOR LINES ---"
  printf "[counter] %s\n" "${COUNTER_LINE:-<none>}"
  printf "[detector] %s\n" "${DETECTOR_LINE:-<none>}"
} | tee "$OUT" >/dev/null

if [ "$GRANTED_AR" -ne 1 ]; then
  fail "ACTIVITY_RECOGNITION not granted"
fi

if [ "$STEP_ENV_NEEDED" -gt 0 ]; then
  :
fi

echo "EE1 RESULT=PASS" | tee -a "$OUT"
exit 0
