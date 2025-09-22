#!/bin/sh
PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/_repo/csv_audit.2.txt"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "CSV-AUDIT RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "CSV-AUDIT RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

FILES="$(adb exec-out run-as "$PKG" sh -c 'ls -1 files/*.csv 2>/dev/null' | tr -d '\r')"
[ -n "$FILES" ] || { echo "CSV-AUDIT RESULT=FAIL (no csv files)" | tee "$OUT"; exit 4; }

fail=0
for f in $FILES; do
  hdr="$(adb exec-out run-as "$PKG" head -n1 "$f" 2>/dev/null | tr -d '\r')"
  if [ -z "$hdr" ]; then
    echo "BAD:EMPTY_HEADER $f" | tee -a "$OUT"; fail=$((fail+1)); continue
  fi
  cols="$(printf '%s\n' "$hdr" | awk -F, '{print NF}')"
  adb exec-out run-as "$PKG" awk -F, -v n="$cols" 'NR>1{ if(NF!=n){ exit 1 } }' "$f" >/dev/null 2>&1 \
    || { echo "BAD:COLUMN_MISMATCH $f" | tee -a "$OUT"; fail=$((fail+1)); continue; }
  lf="$(adb exec-out run-as "$PKG" tail -c1 "$f" 2>/dev/null | od -An -t u1 -v | tr -d '[:space:]')" || lf=""
  [ "$lf" = "10" ] || { echo "BAD:NO_TRAILING_LF $f" | tee -a "$OUT"; fail=$((fail+1)); }
  lock="app/locks/$(basename "$f" .csv).header"
  if [ -f "$lock" ]; then
    exp="$(tr -d '\r' < "$lock")"
    [ "$hdr" = "$exp" ] || { echo "BAD:LOCK_MISMATCH $f" | tee -a "$OUT"; fail=$((fail+1)); }
  fi
  echo "OK:$f:$hdr" | tee -a "$OUT"
done

[ "$fail" -eq 0 ] && { echo "CSV-AUDIT RESULT=PASS" | tee -a "$OUT"; exit 0; } || { echo "CSV-AUDIT RESULT=FAIL ($fail)" | tee -a "$OUT"; exit 1; }
