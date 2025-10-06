#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/app_usage_by_category/trigger.txt"
CSV="files/app_category_daily.csv"
RCV="$PKG/.TriggerReceiver"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "TRIGGER RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "TRIGGER RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

ACT="$(grep -R --include='*.kt' -nE '"com\.nick\.myrecoverytracker\.ACTION_[A-Z0-9_]*CATEGORY[A-Z0-9_]*"' app/src/main/java 2>/dev/null | sed -E 's/.*"([^"]+)".*/\1/' | head -n1 || true)"
[ -n "$ACT" ] || { echo "TRIGGER RESULT=FAIL (no CATEGORY action found in code)" | tee "$OUT"; exit 4; }

adb shell logcat -c >/dev/null 2>&1 || true
adb shell cmd activity broadcast -n "$RCV" -a "$ACT" --receiver-foreground --user 0 >/dev/null 2>&1 || true
sleep 2

T="$(adb shell toybox date +%F | tr -d '\r')"
Y="$(adb shell toybox date -d "@$(( $(adb shell toybox date +%s)-86400 ))" +%F | tr -d '\r')"

HDR="$(adb exec-out run-as "$PKG" sed -n '1p' "$CSV" 2>/dev/null | tr -d '\r' || true)"
RT="$(adb exec-out run-as "$PKG" awk -F, -v d="$T" 'NR>1 && $1==d{print}' "$CSV" 2>/dev/null | tr -d '\r' || true)"
RY="$(adb exec-out run-as "$PKG" awk -F, -v d="$Y" 'NR>1 && $1==d{print}' "$CSV" 2>/dev/null | tr -d '\r' || true)"
LOG="$(adb logcat -d 2>/dev/null | grep -i 'AppUsageByCategoryDaily' || true)"

{
  echo "ACTION=$ACT"
  echo "HEADER=${HDR:-MISSING}"
  echo "--- TODAY ($T) ---"
  [ -n "$RT" ] && printf '%s\n' "$RT" || echo "[none]"
  echo "--- YESTERDAY ($Y) ---"
  [ -n "$RY" ] && printf '%s\n' "$RY" || echo "[none]"
  echo "--- LOGCAT ---"
  [ -n "$LOG" ] && printf '%s\n' "$LOG" || echo "[none]"
} | tee "$OUT" >/dev/null

if [ -n "$RT" ] || [ -n "$RY" ]; then
  echo "TRIGGER RESULT=PASS" | tee -a "$OUT"
  exit 0
else
  echo "TRIGGER RESULT=FAIL (no rows for today/yesterday)" | tee -a "$OUT"
  exit 1
fi
