#!/bin/bash
set -euo pipefail
PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/movement_intensity/ee4.txt"

mkdir -p "$(dirname "$OUT")"
exec > >(tee "$OUT") 2>&1

fail(){
  echo "EE4 RESULT=FAIL ($1)"
  exit 1
}

echo "start_ts=$(date -u +%FT%TZ)"
adb get-state >/dev/null 2>&1 || { echo "device_state=<none>"; echo "EE4 RESULT=FAIL (no device)"; exit 2; }
echo "device_state=$(adb get-state 2>/dev/null | tr -d $'\r')"

adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "pm_path=<none>"; echo "EE4 RESULT=FAIL (app not installed)"; exit 3; }
echo "pm_path=$(adb shell pm path "$PKG" 2>/dev/null | tr -d $'\r')"

DOZE_STATE="$(adb shell dumpsys deviceidle get deep 2>/dev/null | tr -d $'\r' || true)"
[ -n "$DOZE_STATE" ] || DOZE_STATE="<unknown>"
echo "doze_deep_state=$DOZE_STATE"

WL_CMD="$(adb shell dumpsys deviceidle whitelist 2>/dev/null | tr -d $'\r' || true)"
WL_ALT="$(adb shell cmd deviceidle whitelist 2>/dev/null | tr -d $'\r' || true)"
WL_DUMP="$(adb shell dumpsys deviceidle 2>/dev/null | tr -d $'\r' || true)"

ALLOW_LINES="$( { printf '%s\n' "$WL_CMD"; printf '%s\n' "$WL_ALT"; printf '%s\n' "$WL_DUMP" | sed -n '/Whitelist/,+120p'; } \
  | grep -i "$PKG" | sort -u || true)"

echo "allow_lines_begin"
[ -n "$ALLOW_LINES" ] && printf '%s\n' "$ALLOW_LINES" || echo "<none>"
echo "allow_lines_end"

if [ -z "$ALLOW_LINES" ]; then
  echo "debug_whitelist_cmd_begin"
  [ -n "$WL_CMD" ] && printf '%s\n' "$WL_CMD" | head -n 80 || echo "<empty>"
  echo "debug_whitelist_cmd_end"
  echo "debug_cmd_deviceidle_whitelist_begin"
  [ -n "$WL_ALT" ] && printf '%s\n' "$WL_ALT" | head -n 80 || echo "<empty>"
  echo "debug_cmd_deviceidle_whitelist_end"
  echo "debug_dumpsys_deviceidle_section_begin"
  printf '%s\n' "$WL_DUMP" | sed -n '/Whitelist/,+120p' | head -n 120
  echo "debug_dumpsys_deviceidle_section_end"
  fail "package not found in deviceidle whitelist"
fi

echo "EE4 RESULT=PASS"
exit 0
