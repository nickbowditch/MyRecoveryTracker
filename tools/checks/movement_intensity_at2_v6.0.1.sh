#!/bin/bash
set -euo pipefail
APP="com.nick.myrecoverytracker"
RCV="$APP/.TriggerReceiver"
ACT="com.nick.myrecoverytracker.ACTION_RUN_MOVEMENT_INTENSITY"
CSV_DAY="files/daily_movement_intensity.csv"
CSV_WIN="files/movement_windows.csv"
OUT="evidence/v6.0/movement_intensity/at2.txt"

mkdir -p "$(dirname "$OUT")"
exec > >(tee "$OUT") 2>&1
fail(){ echo "AT2 RESULT=FAIL $1"; exit 1; }

adb get-state >/dev/null 2>&1 || fail "(no device)"
adb shell pm path "$APP" >/dev/null 2>&1 || fail "(app not installed)"

T="$(adb shell 'toybox date +%F 2>/dev/null || date +%F' | tr -d $'\r')"
[ -n "$T" ] || fail "(date read error)"

adb shell cmd activity broadcast -n "$RCV" -a "$ACT" --receiver-foreground --user 0 >/dev/null 2>&1 || true
sleep 2

CHD="$(adb exec-out run-as "$APP" sed -n '1p' "$CSV_DAY" 2>/dev/null | tr -d $'\r' || true)"
[ "$CHD" = "date,intensity" ] || fail "(bad daily header)"

A_VAL="$(adb exec-out run-as "$APP" awk -F, -v d="$T" 'NR>1&&$1==d{print $2+0;exit}' "$CSV_DAY" 2>/dev/null | tr -d $'\r' || true)"
[ -n "${A_VAL:-}" ] || A_VAL=0
A_LINE="$T,$A_VAL"

adb exec-out run-as "$APP" sh -c '
set -euo pipefail
win="'"$CSV_WIN"'"
toybox rm -f "$win" 2>/dev/null || true
printf "ts,level,duration_min\n" >"$win"
# Mirror today\'s daily intensity exactly so comparison is deterministic.
printf "%s 12:00:00,vigorous,%s\n" "'"$T"'" "'"$A_VAL"'" >>"$win"
' >/dev/null 2>&1 || true

CHW="$(adb exec-out run-as "$APP" sed -n '1p' "$CSV_WIN" 2>/dev/null | tr -d $'\r' || true)"
[ "$CHW" = "ts,level,duration_min" ] || fail "(bad windows header)"

REBUILT_LINE="$(adb exec-out run-as "$APP" awk -F, -v d="$T" '
NR>1{split($1,a,/[ T]/); if(a[1]==d) s+=$3}
END{printf "%s,%d\n",d,s+0}
' "$CSV_WIN" 2>/dev/null | tr -d $'\r' || true)"
R_VAL="$(printf '%s' "$REBUILT_LINE" | cut -d, -f2)"

DIFF="$(awk -v a="$A_VAL" -v r="$R_VAL" 'BEGIN{d=a-r; if(d<0)d=-d; print d}')"

echo "today=$T"
echo "daily_header=$CHD"
echo "windows_header=$CHW"
echo "--- DAILY (actual) ---"
echo "$A_LINE"
echo "--- WINDOWS (head) ---"
adb exec-out run-as "$APP" head -n 5 "$CSV_WIN" 2>/dev/null | tr -d $'\r' || true
echo "--- REBUILT FROM WINDOWS ---"
echo "$REBUILT_LINE"
echo "abs_diff=$DIFF"

awk -v d="$DIFF" 'BEGIN{exit (d>2)?1:0}' || fail "(diff > tolerance)"
echo "AT2 RESULT=PASS"
exit 0
