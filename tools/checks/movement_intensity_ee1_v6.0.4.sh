#!/bin/bash
set -euo pipefail

PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/movement_intensity/ee1.txt"
mkdir -p "$(dirname "$OUT")"
exec > >(tee "$OUT") 2>&1

fail() {
  echo "EE1 RESULT=FAIL ($1)"
  echo "--- DEBUG: device/app ---"
  { adb get-state && adb shell pm path "$PKG"; } 2>&1 | tr -d $'\r' || true
  echo "--- DEBUG: dumpsys package (filtered) ---"
  adb shell dumpsys package "$PKG" 2>/dev/null | tr -d $'\r' | grep -E 'ACTIVITY_RECOGNITION|uses-feature' || true
  echo "--- DEBUG: appops (activity recognition) ---"
  { adb shell appops get "$PKG" ACTIVITY_RECOGNITION 2>/dev/null; adb shell appops get "$PKG" android:activity_recognition 2>/dev/null; } | tr -d $'\r' || true
  echo "--- DEBUG: sensorservice (step sensors) ---"
  adb shell dumpsys sensorservice 2>/dev/null | tr -d $'\r' | awk '
    BEGIN{s=0;c=0}
    /Sensor List/ {s=1}
    s==1 && /(step[_ ]?counter|android\.sensor\.step_counter|step[_ ]?detector|android\.sensor\.step_detector)/ {print; c++}
    END{if(c==0) print "<no step sensor lines found in Sensor List>"}' || true
  exit 1
}

echo "[INFO] EE1 — Activity Recognition + Usage/Health sensors permitted"
adb get-state >/dev/null 2>&1 || { echo "EE1 RESULT=FAIL (no device)"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "EE1 RESULT=FAIL (app not installed)"; exit 3; }

PDUMP="$(adb shell dumpsys package "$PKG" 2>/dev/null | tr -d $'\r' || true)"
AOP="$( { adb shell appops get "$PKG" ACTIVITY_RECOGNITION 2>/dev/null; adb shell appops get "$PKG" android:activity_recognition 2>/dev/null; } | tr -d $'\r' || true)"

GRANTED_AR=0
if printf "%s\n" "$PDUMP" | grep -Eq 'android\.permission\.ACTIVITY_RECOGNITION:[[:space:]]*granted=true|[^A-Z]ACTIVITY_RECOGNITION:[[:space:]]*granted=true'; then
  GRANTED_AR=1
else
  if printf "%s\n" "$AOP" | grep -Eiq 'mode=(allow|allow_fg|foreground|allowed)'; then
    GRANTED_AR=1
  fi
fi
[ "$GRANTED_AR" -eq 1 ] || fail "ACTIVITY_RECOGNITION not granted"

REQUIRE_STEPS=0
if printf "%s\n" "$PDUMP" | grep -Eiq 'uses-feature.*(sensor\.step[_-]?counter|sensor\.step[_-]?detector)'; then
  REQUIRE_STEPS=1
fi

SENS="$(adb shell dumpsys sensorservice 2>/dev/null | tr -d $'\r' || true)"
COUNTER_LINE="$(printf "%s\n" "$SENS" | awk '/Sensor List/{sl=1} sl && /(step[_ ]?counter|android\.sensor\.step_counter)/ {print; exit}')"
DETECTOR_LINE="$(printf "%s\n" "$SENS" | awk '/Sensor List/{sl=1} sl && /(step[_ ]?detector|android\.sensor\.step_detector)/ {print; exit}')"
HAS_COUNTER=$([ -n "$COUNTER_LINE" ] && echo 1 || echo 0)
HAS_DETECTOR=$([ -n "$DETECTOR_LINE" ] && echo 1 || echo 0)

echo "pkg=$PKG"
echo "activity_recognition_granted=$GRANTED_AR"
echo "requires_step_features=$REQUIRE_STEPS"
echo "step_counter_present=$HAS_COUNTER"
echo "step_detector_present=$HAS_DETECTOR"
echo "--- PACKAGE PERMS (filtered) ---"
printf "%s\n" "$PDUMP" | grep -E 'ACTIVITY_RECOGNITION' || true
echo "--- PACKAGE FEATURES (filtered) ---"
printf "%s\n" "$PDUMP" | grep -E 'uses-feature' | grep -Ei 'step|sensor' || true
echo "--- SENSOR LINES ---"
printf "[counter] %s\n" "${COUNTER_LINE:-<none>}"
printf "[detector] %s\n" "${DETECTOR_LINE:-<none>}"

if [ "$REQUIRE_STEPS" -eq 1 ] && [ $((HAS_COUNTER+HAS_DETECTOR)) -eq 0 ]; then
  fail "app declares step sensor feature(s) but no runtime step sensors available"
fi

echo "EE1 RESULT=PASS"
exit 0
