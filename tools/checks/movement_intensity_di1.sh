#!/bin/bash
set -euo pipefail
PKG="com.nick.myrecoverytracker"
CSV_DAY="files/daily_movement_intensity.csv"
OUT="evidence/v6.0/movement_intensity/di1.txt"

mkdir -p "$(dirname "$OUT")"
exec > >(tee "$OUT") 2>&1
fail(){ echo "DI1 RESULT=FAIL ($1)"; exit 1; }

adb get-state >/dev/null 2>&1 || { echo "DI1 RESULT=FAIL (no device)"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "DI1 RESULT=FAIL (app not installed)"; exit 3; }

HDR_DEVICE="$(adb exec-out run-as "$PKG" sed -n '1p' "$CSV_DAY" 2>/dev/null | tr -d '\r' || true)"
[ -n "$HDR_DEVICE" ] || fail "daily CSV missing or unreadable"

LOCK=""
for L in \
  tools/locks/daily_movement_intensity.header \
  tools/schema_locks/daily_movement_intensity.header \
  tools/locks/movement_intensity.header \
  locks/daily_movement_intensity.header \
  .schema_lock/daily_movement_intensity.header \
  schema.lock/daily_movement_intensity.header \
  schema_locks/daily_movement_intensity.header
do
  if [ -f "$L" ]; then
    LOCK="$(tr -d '\r' < "$L")"
    LOCK_PATH="$L"
    break
  fi
done

if [ -z "${LOCK:-}" ]; then
  LOCK="date,intensity"
  LOCK_PATH="<builtin-default>"
fi

echo "lock_path=$LOCK_PATH"
echo "lock_value=$LOCK"
echo "device_header=$HDR_DEVICE"

if [ "$HDR_DEVICE" != "$LOCK" ]; then
  echo "--- DEBUG: device CSV head ---"
  adb exec-out run-as "$PKG" sh -c 'head -n 5 "'"$CSV_DAY"'" 2>/dev/null | tr -d "\r"' || true
  fail "header mismatch"
fi

echo "DI1 RESULT=PASS"
exit 0
