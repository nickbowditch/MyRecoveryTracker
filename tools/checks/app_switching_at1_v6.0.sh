#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
CSV="files/daily_app_switching.csv"
OUT_DIR="evidence/v6.0/app_switching"
OUT="$OUT_DIR/at1.txt"
RCV="$PKG/.TriggerReceiver"
ACT="$PKG.ACTION_RUN_APP_SWITCHING_DAILY"
EXP="date,switches,entropy"
mkdir -p "$OUT_DIR"

fail(){ echo "AT-1 RESULT=FAIL $1" | tee "$OUT"; exit 1; }

adb get-state >/dev/null 2>&1 || fail "(no device)"
adb shell pm path "$PKG" >/dev/null 2>&1 || fail "(app not installed)"

HDR="$(adb exec-out run-as "$PKG" sed -n '1p' "$CSV" 2>/dev/null | tr -d '\r' || true)"
if [ -z "$HDR" ]; then
adb shell run-as "$PKG" sh -c 'mkdir -p files; echo "date,switches,entropy" > files/daily_app_switching.csv' >/dev/null 2>&1 || true
HDR="$(adb exec-out run-as "$PKG" sed -n '1p' "$CSV" 2>/dev/null | tr -d '\r' || true)"
fi
[ "$HDR" = "$EXP" ] || fail "(bad/mismatched csv header)"

T="$(adb shell toybox date +%F 2>/dev/null | tr -d '\r')"

adb exec-out run-as "$PKG" sh -c '
set -eu
f="'"$CSV"'"; d="'"$T"'"
tmp="$f.tmp"
{ head -n1 "$f"; tail -n +2 "$f" | awk -F, -v d="$T" "!(\$1==d)"; } > "$tmp" 2>/dev/null || { echo "date,switches,entropy" > "$tmp"; }
mv "$tmp" "$f"
' >/dev/null 2>&1 || true

PRE_HASH="$(adb exec-out run-as "$PKG" cat "$CSV" 2>/dev/null | sha1sum 2>/dev/null | awk '{print $1}' || true)"
[ -n "$PRE_HASH" ] || PRE_HASH="MISSING"

adb shell cmd activity broadcast -n "$RCV" -a "$ACT" --receiver-foreground --user 0 >/dev/null 2>&1 || fail "(broadcast failed)"

deadline=$(( $(date +%s) + 25 ))
ROW=""
while :; do
ROW="$(adb exec-out run-as "$PKG" awk -F, -v d="$T" 'NR>1 && $1==d{print; found=1} END{if(!found) exit 1}' "$CSV" 2>/dev/null | tr -d '\r' || true)"
[ -n "$ROW" ] && break
[ "$(date +%s)" -ge "$deadline" ] && break
sleep 1
done
[ -n "$ROW" ] || fail "(today rows missing)"

CHK_OK="$(printf '%s\n' "$ROW" | awk -F, '
function isnum(x){ return x ~ /^[0-9]+([.][0-9]+)?$/ }
BEGIN{ok=1}
{
s=$2; e=$3
if(!isnum(s)) ok=0
else if(s<0 || s>10000) ok=0
if(!isnum(e)) ok=0
else if(e<0 || e>3600000) ok=0
}
END{ exit ok?0:1 }' && echo OK || echo BAD)"
[ "$CHK_OK" = "OK" ] || fail "(switch/entropy invalid/out of range)"

POST_HASH="$(adb exec-out run-as "$PKG" cat "$CSV" 2>/dev/null | sha1sum 2>/dev/null | awk '{print $1}' || true)"
[ -n "$POST_HASH" ] || fail "(csv unreadable post-run)"
[ "$POST_HASH" != "$PRE_HASH" ] || fail "(csv unchanged after trigger)"

{
echo "ACTION=$ACT"
echo "DATE=$T"
echo "ROWS_TODAY=$(printf '%s\n' "$ROW" | wc -l | awk '{print $1}')"
echo "AT-1 RESULT=PASS"
} | tee "$OUT"
exit 0
