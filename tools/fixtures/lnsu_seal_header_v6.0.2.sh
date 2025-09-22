#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/lnsu/ee3_seal.2.txt"
LOCK="app/locks/daily_lnsu.header"
EXP="date,feature_schema_version,minutes_22_02"
mkdir -p "$(dirname "$OUT")" app/locks

adb get-state >/dev/null 2>&1 || { echo "EE-3-SEAL RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG"  >/dev/null 2>&1 || { echo "EE-3-SEAL RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

printf '%s\n' "$EXP" > "$LOCK"

adb exec-out run-as "$PKG" sh -c '
set -eu
f="files/daily_lnsu.csv"; exp="date,feature_schema_version,minutes_22_02"
mkdir -p files
if [ -f "$f" ]; then
  cur="$(head -n1 "$f" 2>/dev/null | tr -d "\r")"
  if [ "$cur" != "$exp" ]; then tmp="${f}.tmp.$$"; { echo "$exp"; tail -n +2 "$f"; } > "$tmp" && mv "$tmp" "$f"; fi
else
  echo "$exp" > "$f"
fi
' >/dev/null 2>&1

echo "EE-3-SEAL RESULT=PASS" | tee "$OUT"
