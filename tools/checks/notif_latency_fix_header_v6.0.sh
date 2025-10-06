#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
CSV="files/daily_notification_latency.csv"
HDR="date,feature_schema_version,p50_ms,p90_ms,p99_ms,count"
OUT="evidence/v6.0/notification_latency/fix_header.txt"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "FIX RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "FIX RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

EXISTS="$(adb shell run-as "$PKG" sh -c '[ -f "'"$CSV"'" ] && echo yes || echo no' 2>/dev/null || true)"
if [ "$EXISTS" != "yes" ]; then
  adb shell run-as "$PKG" sh -c 'mkdir -p files && printf "%s\n" "'"$HDR"'" > "'"$CSV"'"' >/dev/null 2>&1 || true
else
  CUR="$(adb shell run-as "$PKG" head -n1 "$CSV" 2>/dev/null | tr -d '\r' || true)"
  if [ "$CUR" != "$HDR" ]; then
    adb shell run-as "$PKG" sh -c '
      f="'"$CSV"'"; t="$(toybox mktemp 2>/dev/null || mktemp)"
      { echo "'"$HDR"'"; (toybox tail -n +2 "$f" 2>/dev/null || tail -n +2 "$f" 2>/dev/null || true); } > "$t" && mv "$t" "$f"
    ' >/dev/null 2>&1 || true
  fi
fi

adb shell run-as "$PKG" head -n1 "$CSV" 2>/dev/null | tee "$OUT" || true
echo "FIX RESULT=OK" | tee -a "$OUT"
