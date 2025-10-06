#!/bin/sh
set -eu
APP="com.nick.myrecoverytracker"
RAW="files/usage_events.csv"
DAILY="files/daily_usage_events.csv"
OUT_DIR="evidence/v6.0/usage_events"
OUT="$OUT_DIR/at2.txt"
mkdir -p "$OUT_DIR"
fail(){ echo "AT2 RESULT=FAIL $1" | tee "$OUT"; exit 1; }

adb get-state >/dev/null 2>&1 || fail "(no device)"
adb shell pm path "$APP" >/dev/null 2>&1 || fail "(app not installed)"

RH="$(adb exec-out run-as "$APP" sed -n '1p' "$RAW" 2>/dev/null | tr -d '\r' || true)"
DH="$(adb exec-out run-as "$APP" sed -n '1p' "$DAILY" 2>/dev/null | tr -d '\r' || true)"
[ -n "$RH" ] || fail "(missing raw)"
[ -n "$DH" ] || fail "(missing daily)"
[ "$RH" = "date,time,event_type,package" ] || fail "(bad raw header)"
[ "$DH" = "date,event_count" ] || fail "(bad daily header)"

TODAY="$(adb shell toybox date +%F 2>/dev/null | tr -d '\r')"
YEST="$(adb shell toybox date -d "@$(( $(adb shell toybox date +%s)-86400 ))" +%F 2>/dev/null | tr -d '\r')"

RAW_DATES="$(adb exec-out run-as "$APP" awk -F, 'NR>1{if(length($1)==10)print $1}' "$RAW" 2>/dev/null | sort -u || true)"
DAILY_DATES="$(adb exec-out run-as "$APP" awk -F, 'NR>1{if($1 ~ /^[0-9]{4}-[0-9]{2}-[0-9]{2}$/)print $1}' "$DAILY" 2>/dev/null | sort -u || true)"

TMP1="$(mktemp)"; TMP2="$(mktemp)"; trap 'rm -f "$TMP1" "$TMP2"' EXIT
printf '%s\n' "$RAW_DATES" > "$TMP1"
printf '%s\n' "$DAILY_DATES" > "$TMP2"
INTER="$(grep -Fxf "$TMP1" "$TMP2" | sort -u || true)"

if printf '%s\n' "$INTER" | grep -qx "$YEST"; then TARGET="$YEST"; else TARGET="$(printf '%s\n' "$INTER" | sort | tail -n1 || true)"; fi
[ -n "$TARGET" ] || fail "(no overlapping date between raw and daily)"

RAW_N="$(adb exec-out run-as "$APP" awk -F, -v d="$TARGET" 'NR>1 && $1==d{n++} END{print n+0}' "$RAW" 2>/dev/null || echo 0)"
DAILY_N="$(adb exec-out run-as "$APP" awk -F, -v d="$TARGET" 'NR>1 && $1==d{print $2; exit}' "$DAILY" 2>/dev/null || echo 0)"

if [ "$RAW_N" -eq "$DAILY_N" ]; then
echo "OK  $TARGET raw=$RAW_N daily=$DAILY_N" | tee "$OUT"
echo "AT2 RESULT=PASS" | tee -a "$OUT"
exit 0
else
echo "BAD $TARGET raw=$RAW_N daily=$DAILY_N" | tee "$OUT"
echo "AT2 RESULT=FAIL" | tee -a "$OUT"
exit 1
fi
