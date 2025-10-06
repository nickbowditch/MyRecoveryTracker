#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
RAW="files/usage_events.csv"
DAILY="files/daily_usage_events.csv"
OUT_DIR="evidence/v6.0/usage_events"
OUT="$OUT_DIR/at1.txt"
RCV="$PKG/.TriggerReceiver"
ACT="$PKG.ACTION_RUN_USAGE_EVENTS_DAILY"
EXP="date,event_count"
mkdir -p "$OUT_DIR"

fail(){ echo "AT-1 RESULT=FAIL $1" | tee "$OUT"; exit 1; }

adb get-state >/dev/null 2>&1 || fail "(no device)"
adb shell pm path "$PKG" >/dev/null 2>&1 || fail "(app not installed)"

adb shell run-as "$PKG" sh <<'INP' >/dev/null 2>&1 || true
set -eu
mkdir -p files
[ -f "files/usage_events.csv" ]  || echo "date,time,event_type,package" > "files/usage_events.csv"
[ -f "files/daily_usage_events.csv" ] || echo "date,event_count" > "files/daily_usage_events.csv"
INP

RH="$(adb exec-out run-as "$PKG" sed -n '1p' "$RAW" 2>/dev/null | tr -d '\r' || true)"
DH="$(adb exec-out run-as "$PKG" sed -n '1p' "$DAILY" 2>/dev/null | tr -d '\r' || true)"
[ "$RH" = "date,time,event_type,package" ] || fail "(bad raw header)"
[ "$DH" = "$EXP" ] || fail "(bad daily header)"

TODAY="$(adb shell toybox date +%F 2>/dev/null | tr -d '\r')"

RAW_N="$(adb exec-out run-as "$PKG" awk -F, -v d="$TODAY" 'NR>1&&$1==d{n++} END{print n+0}' "$RAW" 2>/dev/null || echo 0)"

adb exec-out run-as "$PKG" sh -c '
set -eu
d="'"$TODAY"'"
in="'"$DAILY"'"
tmp="$in.tmp"
[ -f "$in" ] || echo "date,event_count" > "$in"
{
head -n1 "$in"
tail -n +2 "$in" | awk -F, -v d="$d" "!(\$1==d)"
} > "$tmp"
mv "$tmp" "$in"
' >/dev/null 2>&1 || true

adb shell cmd activity broadcast -n "$RCV" -a "$ACT" --receiver-foreground --user 0 >/dev/null 2>&1 || fail "(broadcast failed)"
sleep 2

ROW="$(adb exec-out run-as "$PKG" awk -F, -v d="$TODAY" 'NR>1&&$1==d{print;exit}' "$DAILY" 2>/dev/null | tr -d '\r' || true)"
[ -n "$ROW" ] || fail "(today row missing)"
CNT="$(printf '%s\n' "$ROW" | awk -F, '{print $2}')"
printf '%s' "$CNT" | grep -Eq '^[0-9]+$' || fail "(event_count not integer)"
awk -v c="$CNT" 'BEGIN{exit !(c>=0 && c<=500000)}' || fail "(event_count out of bounds)"

if [ "$CNT" -eq "$RAW_N" ]; then
{
echo "RAW_N=$RAW_N"
echo "DAILY_N=$CNT"
echo "AT-1 RESULT=PASS"
} | tee "$OUT"
exit 0
else
{
echo "RAW_N=$RAW_N"
echo "DAILY_N=$CNT"
echo "AT-1 RESULT=FAIL (raw vs daily mismatch)"
} | tee "$OUT"
exit 1
fi
