#!/bin/bash
set -euo pipefail

PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/movement_intensity/ee1.txt"
mkdir -p "$(dirname "$OUT")"

fail() {
  echo "EE1 RESULT=FAIL ($1)" | tee "$OUT"
  echo "--- DEBUG: device/app presence ---" | tee -a "$OUT"
  { adb get-state && adb shell pm path "$PKG"; } 2>&1 | tr -d $'\r' | tee -a "$OUT" || true
  echo "--- DEBUG: dumpsys package (perm + features slice) ---" | tee -a "$OUT"
  adb shell dumpsys package "$PKG" 2>/dev/null | tr -d $'\r' | awk '
    /requested permissions:/,/install permissions:/ {print}
    /uses-feature/ {print}
  ' | tee -a "$OUT" || true
  echo "--- DEBUG: appops (ACTIVITY_RECOGNITION variants) ---" | tee -a "$OUT"
  { adb shell appops get "$PKG" ACTIVITY_RECOGNITION 2>/dev/null; adb shell appops get "$PKG" android:activity_recognition 2>/dev/null; } | tr -d $'\r' | tee -a "$OUT" || true
  echo "--- DEBUG: additional appops (usage/health related) ---" | tee -a "$OUT"
  { adb shell appops get "$PKG" GET_USAGE_STATS 2>/dev/null; adb shell appops get "$PKG" BODY_SENSORS 2>/dev/null; } | tr -d $'\r' | tee -a "$OUT" || true
  echo "--- DEBUG: sensorservice step sensors ---" | tee -a "$OUT"
  adb shell dumpsys sensorservice 2>/dev/null | tr -d $'\r' | awk '
    BEGIN{s=0;c=0}
    /Sensor List/ {s=1}
    s==1 && /(step[_ ]?counter|android\.sensor\.step_counter|step[_ ]?detector|android\.sensor\.step_detector)/ {print; c++}
    END{if(c==0) print "<no step sensor lines found in Sensor List>"}' | tee -a "$OUT" || true
  echo "--- DEBUG: cmd sensor list (fallback) ---" | tee -a "$OUT"
  adb shell cmd sensor list 2>/dev/null | tr -d $'\r' | grep -Ei 'step[_ ]?counter|step[_ ]?detector' | tee -a "$OUT" || true
  exit 1
}

adb get-state >/dev/null 2>&1 || { echo "EE1 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "EE1 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

PDUMP="$(adb shell dumpsys package "$PKG" 2>/dev/null | tr -d $'\r' || true)"

GRANTED_AR=0
if printf "%s\n" "$PDUMP" | grep -Eq 'android\.permission\.ACTIVITY_RECOGNITION:[[:space:]]*granted=true|[^A-Z]ACTIVITY_RECOGNITION:[[:space:]]*granted=true'; then
  GRANTED_AR=1
else
  AOP="$( { adb shell appops get "$PKG" ACTIVITY_RECOGNITION 2>/dev/null; adb shell appops get "$PKG" android:activity_recognition 2>/dev/null; } | tr -d $'\r' || true)"
  if printf "%s\n" "$AOP" | grep -Eiq 'mode=(allow|allow_fg|foreground|allowed)'; then
    GRANTED_AR=1
  fi
fi

REQUIRE_STEPS=0
if printf "%s\n" "$PDUMP" | grep -Eiq 'uses-feature.*(sensor\.step[_-]?counter|sensor\.step[_-]?detector)'; then
  REQUIRE_STEPS=1
fi

SENS_DUMP="$(adb shell dumpsys sensorservice 2>/dev/null | tr -d $'\r' || true)"
COUNTER_LINE="$(printf "%s\n" "$SENS_DUMP" | awk '/Sensor List/{sl=1} sl && /(step[_ ]?counter|android\.sensor\.step_counter)/ {print; exit}')"
DETECTOR_LINE="$(printf "%s\n" "$SENS_DUMP" | awk '/Sensor List/{sl=1} sl && /(step[_ ]?detector|android\.sensor\.step_detector)/ {print; exit}')"
HAS_COUNTER=$([ -n "$COUNTER_LINE" ] && echo 1 || echo 0)
HAS_DETECTOR=$([ -n "$DETECTOR_LINE" ] && echo 1 || echo 0)

if [ "$HAS_COUNTER" -eq 0 ] && [ "$HAS_DETECTOR" -eq 0 ]; then
  ALT="$(adb shell cmd sensor list 2>/dev/null | tr -d $'\r' || true)"
  if printf "%s\n" "$ALT" | grep -Eiq 'step[_ ]?counter'; then HAS_COUNTER=1; fi
  if printf "%s\n" "$ALT" | grep -Eiq 'step[_ ]?detector'; then HAS_DETECTOR=1; fi
fi

{
  echo "EE1 CHECK: Activity Recognition + Usage/Health sensors permitted"
  echo "pkg=$PKG"
  echo "activity_recognition_granted=$GRANTED_AR"
  echo "requires_step_features=$REQUIRE_STEPS"
  echo "step_counter_present=$HAS_COUNTER"
  echo "step_detector_present=$HAS_DETECTOR"
  echo "--- PACKAGE PERMS (filtered) ---"
  printf "%s\n" "$PDUMP" | grep -E 'ACTIVITY_RECOGNITION' || true
  echo "--- PACKAGE FEATURES (filtered) ---"
  printf "%s\n" "$PDUMP" | grep -E 'uses-feature' | grep -Ei 'step|sensor' || true
  echo "--- SENSOR LINES (dumpsys) ---"
  printf "[counter] %s\n" "${COUNTER_LINE:-<none>}"
  printf "[detector] %s\n" "${DETECTOR_LINE:-<none>}"
} | tee "$OUT" >/dev/null

[ "$GRANTED_AR" -eq 1 ] || fail "ACTIVITY_RECOGNITION not granted"
if [ "$REQUIRE_STEPS" -eq 1 ] && [ $((HAS_COUNTER+HAS_DETECTOR)) -eq 0 ]; then
  fail "App declares step sensor feature(s) but no runtime step sensors available"
fi

echo "EE1 RESULT=PASS" | tee -a "$OUT"
exit 0
