#!/bin/bash
set -euo pipefail

PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/distance/ee4.txt"
TARGET_DIR="evidence/v6.0/distance"
mkdir -p "$(dirname "$OUT")"
exec > >(tee "$OUT") 2>&1

fail() {
  echo "[FAIL] $1"
  echo "[DEBUG] --- deviceidle whitelist dump (filtered) ---"
  adb shell dumpsys deviceidle whitelist 2>/dev/null | grep -E "$PKG|distance" || true
  echo "RESULT=FAIL"
  exit 1
}

echo "[INFO] EE4 evidence/ whitelisted"
adb get-state | grep -q "^device$" || fail "No device connected"
adb shell pm path "$PKG" >/dev/null 2>&1 || fail "App $PKG not installed"

echo "[INFO] Checking idle whitelist entries for $PKG"
WL_RAW="$(adb shell dumpsys deviceidle whitelist 2>/dev/null | tr -d '\r' || true)"
[[ -n "${WL_RAW// /}" ]] || fail "Could not read whitelist"

echo "[INFO] Matching allow-lines for evidence/v6.0/distance/"
MATCHES="$(printf '%s\n' "$WL_RAW" | grep -E "$PKG" || true)"

{
  echo "=== Raw whitelist dump (head) ==="
  printf "%s\n" "$WL_RAW" | head -n 40
  echo
  echo "=== Filtered entries for $PKG ==="
  printf "%s\n" "${MATCHES:-<none>}"
} | tee "$OUT" >/dev/null

[[ -n "${MATCHES// /}" ]] || fail "No whitelist entries found for $PKG"
echo "[PASS] $PKG present in idle whitelist (evidence/ access allowed)"
echo "RESULT=PASS"
exit 0
