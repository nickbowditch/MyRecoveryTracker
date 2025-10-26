#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/_repo/sync_headers.txt"
mkdir -p "$(dirname "$OUT")"
exec > >(tee "$OUT") 2>&1

adb get-state >/dev/null 2>&1 || { echo "no device"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "app not installed"; exit 3; }

LIST="$(adb exec-out run-as "$PKG" sh -c 'ls -1 files/*.csv 2>/dev/null || true' | tr -d "\r" | sort -u)"
echo "SYNC_HEADERS_FROM_DEVICE"
printf '%s\n' "$LIST" | while IFS= read -r f; do
  base="$(basename "$f" .csv)"
  hdr="$(adb exec-out run-as "$PKG" sed -n '1p' "$f" 2>/dev/null | tr -d '\r' || true)"
  [ -n "$hdr" ] || hdr="UNKNOWN"
  printf '%s\n' "$hdr" > "app/locks/$base.header"
  echo "wrote app/locks/$base.header"
done
echo "DONE"
