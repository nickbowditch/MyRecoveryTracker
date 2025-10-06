#!/bin/bash
set -euo pipefail
PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/distance/ee1.txt"
mkdir -p "$(dirname "$OUT")"
exec > >(tee "$OUT") 2>&1

echo "[INFO] Checking device connection..."
adb get-state | grep -q "device" || { echo "[FAIL] No device connected."; exit 1; }

echo "[INFO] Checking app installation..."
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "[FAIL] App $PKG not installed."; exit 1; }

echo "[INFO] Checking location permissions..."
PERMS="$(adb shell dumpsys package "$PKG" | grep -E 'android.permission.ACCESS_(FINE|COARSE|BACKGROUND)_LOCATION' | tr -d '\r')"
echo "$PERMS"

FINE_OK="$(echo "$PERMS" | grep -q 'ACCESS_FINE_LOCATION: granted=true' && echo 1 || echo 0)"
COARSE_OK="$(echo "$PERMS" | grep -q 'ACCESS_COARSE_LOCATION: granted=true' && echo 1 || echo 0)"
BG_OK="$(echo "$PERMS" | grep -q 'ACCESS_BACKGROUND_LOCATION: granted=true' && echo 1 || echo 0)"

if [[ "$FINE_OK" -eq 1 && "$COARSE_OK" -eq 1 ]]; then
  echo "[DEBUG] Fine and Coarse location permissions granted."
else
  echo "[FAIL] Fine and/or Coarse location permission not granted."
  echo "[DEBUG] Permissions dump:"
  echo "$PERMS"
  echo "RESULT=FAIL"
  exit 1
fi

if echo "$PERMS" | grep -q 'ACCESS_BACKGROUND_LOCATION'; then
  if [[ "$BG_OK" -eq 0 ]]; then
    echo "[FAIL] Background location permission declared but not granted."
    echo "[DEBUG] Permissions dump:"
    echo "$PERMS"
    echo "RESULT=FAIL"
    exit 1
  fi
fi

echo "[INFO] Checking idle whitelist (if applicable)..."
if adb shell dumpsys deviceidle whitelist | grep -q "$PKG"; then
  echo "[DEBUG] App is in idle whitelist."
else
  echo "[WARN] App not in idle whitelist (may affect background location updates)."
fi

echo "RESULT=PASS"
exit 0
