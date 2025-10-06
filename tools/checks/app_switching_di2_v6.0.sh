#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
CSV="files/daily_app_switching.csv"
OUT_DIR="evidence/v6.0/app_switching"
OUT="$OUT_DIR/di2.txt"
DUP_OUT="$OUT_DIR/di2.dups.txt"
mkdir -p "$OUT_DIR"

adb get-state >/dev/null 2>&1 || { echo "DI-2 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "DI-2 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

HDR="$(adb exec-out run-as "$PKG" head -n1 "$CSV" 2>/dev/null | tr -d '\r' || true)"
[ -n "$HDR" ] || { echo "DI-2 RESULT=FAIL (missing csv)" | tee "$OUT"; exit 4; }
[ "$HDR" = "date,switches,entropy" ] || {
  {
    echo "DI-2 RESULT=FAIL (bad header)"
    echo "--- DEBUG ---"
    echo "EXPECTED: date,switches,entropy"
    echo "ACTUAL:   ${HDR:-[empty]}"
  } | tee "$OUT"
  exit 5
}

adb exec-out run-as "$PKG" sh -c '
set -eu
f="'"$CSV"'"
tail -n +2 "$f" 2>/dev/null \
| awk -F, "NF>=1{d=\$1; gsub(/^[[:space:]]+|[[:space:]]+$/, \"\", d); if(d!=\"\") cnt[d]++} END{for(d in cnt) if(cnt[d]>1) print d\",\"cnt[d]}" \
| toybox tr -d "\r"
' 2>/dev/null | sort > "$DUP_OUT" || :

DUPS_COUNT=0
[ -s "$DUP_OUT" ] && DUPS_COUNT="$(awk -F, '{s+=($2-1)} END{print s+0}' "$DUP_OUT")"

if [ "${DUPS_COUNT:-0}" -eq 0 ]; then
  echo "DI-2 RESULT=PASS" | tee "$OUT"
  exit 0
else
  {
    echo "DI-2 RESULT=FAIL (duplicate dates: $DUPS_COUNT)"
    echo "--- DEBUG: DUPLICATE DATES (date,count) ---"
    head -n 20 "$DUP_OUT" || echo "[none]"
    echo "--- DEBUG: CSV HEAD ---"
    adb exec-out run-as "$PKG" head -n 10 "$CSV" 2>/dev/null | tr -d "\r" || echo "[unreadable]"
  } | tee "$OUT"
  exit 6
fi
