#!/bin/sh
set -eu

OUT="evidence/v6.0/_repo/remove_sleep_foreground.txt"
mkdir -p "$(dirname "$OUT")"

SVC="app/src/main/java/com/nick/myrecoverytracker/ForegroundSleepService.kt"
[ -f "$SVC" ] || { : >"$OUT"; exit 0; }

sed_ip() { sed -i '' -E "$1" "$2" 2>/dev/null || sed -i -E "$1" "$2"; }

cp "$SVC" "$SVC.bak" 2>/dev/null || true

sed_ip 's/\.setContentText\(\s*"Sleep processing active"\s*\)/.setContentText("")/g' "$SVC"
sed_ip 's/\.setContentTitle\(\s*"Sleep processing active"\s*\)/.setContentTitle("")/g' "$SVC"
sed_ip 's/\.setContentText\(\s*getString\(\s*R\.string\.sleep_processing_active\s*\)\s*\)/.setContentText("")/g' "$SVC"
sed_ip 's/\.setContentTitle\(\s*getString\(\s*R\.string\.sleep_processing_active\s*\)\s*\)/.setContentTitle("")/g' "$SVC"
sed_ip 's/^([[:space:]]*)startForeground\(/\1\/\/startForeground(/' "$SVC"

grep -nH -E 'Sleep processing active|R\.string\.sleep_processing_active|startForeground\(' "$SVC" || true >"$OUT"

git add "$SVC" >/dev/null 2>&1 || true
git commit -m "Remove sleep foreground notification" >/dev/null 2>&1 || true
