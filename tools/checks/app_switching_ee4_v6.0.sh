#!/bin/sh
set -eu

OUT="evidence/v6.0/app_switching/ee4.txt"
mkdir -p "$(dirname "$OUT")"

if [ ! -f .gitignore ]; then
  echo "EE-4 RESULT=FAIL (.gitignore missing)" | tee "$OUT"
  exit 1
fi

ALLOW_OK="$(grep -E '^!evidence/v6\.0/app_switching/?$' .gitignore || true)"
DENY_OK="$(grep -E '^evidence/' .gitignore | grep -v 'app_switching' || true)"

{
echo "=== .gitignore ALLOW LINE ==="
[ -n "$ALLOW_OK" ] && echo "$ALLOW_OK" || echo "[missing]"
echo
echo "=== OTHER evidence/ RULES ==="
[ -n "$DENY_OK" ] && echo "$DENY_OK" || echo "[none]"
} | tee "$OUT" >/dev/null

if [ -n "$ALLOW_OK" ]; then
  echo "EE-4 RESULT=PASS" | tee -a "$OUT"
  exit 0
else
  echo "EE-4 RESULT=FAIL" | tee -a "$OUT"
  exit 1
fi
