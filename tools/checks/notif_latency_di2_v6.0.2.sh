#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
CSV="files/daily_notification_latency.csv"
LOCK="app/locks/daily_notif_latency.header"
OUT_DIR="evidence/v6.0/notification_latency"
OUT="$OUT_DIR/di2.1.txt"
DUP_OUT="$OUT_DIR/di2.dups.1.txt"
mkdir -p "$OUT_DIR"

DEV="$(adb devices 2>/dev/null | awk 'NR>1 && $2=="device"{print $1; exit}')"
[ -n "${DEV:-}" ] || { echo "DI-2 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }

adb -s "$DEV" shell pm path "$PKG" >/dev/null 2>&1 || { echo "DI-2 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

EXP="$(tr -d '\r' < "$LOCK" 2>/dev/null || true)"
[ -n "$EXP" ] || { echo "DI-2 RESULT=FAIL (missing lock)" | tee "$OUT"; exit 4; }

HDR="$(adb -s "$DEV" exec-out run-as "$PKG" head -n1 "$CSV" 2>/dev/null | tr -d '\r' || true)"
[ -n "$HDR" ] || { echo "DI-2 RESULT=FAIL (missing csv)" | tee "$OUT"; exit 5; }
[ "$HDR" = "$EXP" ] || { echo "DI-2 RESULT=FAIL (bad header)" | tee "$OUT"; exit 6; }

adb -s "$DEV" exec-out run-as "$PKG" sh -c '
awk -F, "
NR==1{next}
{
gsub(/^[[:space:]]+|[[:space:]]+$/, \"\", \$1);
if(\$1!=\"\" ){ c[\$1]++ }
}
END{
for(k in c) if(c[k]>1) print k \",\" c[k]
}
" "'"$CSV"'"' 2>/dev/null | tr -d '\r' | sort > "$DUP_OUT" || :


DUPS_COUNT=0
[ -s "$DUP_OUT" ] && DUPS_COUNT="$(awk -F, '{s+=($2-1)} END{print s+0}' "$DUP_OUT")"

[ "${DUPS_COUNT:-0}" -eq 0 ] || { echo "DI-2 RESULT=FAIL (duplicate dates: $DUPS_COUNT)" | tee "$OUT"; exit 7; }

echo "DI-2 RESULT=PASS" | tee "$OUT"
exit 0
