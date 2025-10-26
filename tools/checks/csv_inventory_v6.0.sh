#!/bin/bash
set -euo pipefail
PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/_repo/csv_inventory.txt"
mkdir -p "$(dirname "$OUT")"
exec > >(tee "$OUT") 2>&1

EXPECTED="$(for h in app/locks/*.header; do [ -f "$h" ] && b="$(basename "$h" .header)" && echo "files/$b.csv"; done | sort -u)"
echo "EXPECTED:"; printf '%s\n' "$EXPECTED"

adb get-state >/dev/null 2>&1 || { echo -e "PRESENT:\n<no device>"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo -e "PRESENT:\n<app not installed>"; exit 3; }

PRESENT="$(adb exec-out run-as "$PKG" sh -c 'ls -1 files/*.csv 2>/dev/null || true' | tr -d $'\r' | sort -u)"
echo "PRESENT:"; if [ -n "$PRESENT" ]; then printf '%s\n' "$PRESENT"; else echo "<none>"; fi

echo "MISSING:"; comm -23 <(printf '%s\n' "$EXPECTED") <(printf '%s\n' "$PRESENT") || true
echo "UNEXPECTED:"; comm -13 <(printf '%s\n' "$EXPECTED") <(printf '%s\n' "$PRESENT") || true
