#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
CSV_DAILY="files/daily_unlocks.csv"
CSV_RAW="files/unlock_log.csv"
EXP_DAILY="date,feature_schema_version,daily_unlocks"
ACT="com.nick.myrecoverytracker.ACTION_RUN_UNLOCK_ROLLUP"
RCV="$PKG/.TriggerReceiver"
OUT_DIR="evidence/v6.0/unlocks"
OUT="$OUT_DIR/at1.txt"
LOG="$OUT_DIR/at1.log.txt"
mkdir -p "$OUT_DIR"

fail(){ echo "AT-1 RESULT=FAIL $1" | tee "$OUT"; exit 1; }

adb get-state >/dev/null 2>&1 || fail "(no device)"
adb shell pm path "$PKG" >/dev/null 2>&1 || fail "(app not installed)"

adb exec-out run-as "$PKG" sh -c '
set -eu
mkdir -p files
[ -f "'"$CSV_RAW"'" ] || printf "ts,event\n" >"'"$CSV_RAW"'"
[ -f "'"$CSV_DAILY"'" ] || printf "'"$EXP_DAILY"'\n" >"'"$CSV_DAILY"'"
' >/dev/null

HDR="$(adb exec-out run-as "$PKG" head -n1 "$CSV_DAILY" 2>/dev/null | tr -d '\r' || true)"
[ "$HDR" = "$EXP_DAILY" ] || fail "(bad daily_unlocks header)"

TODAY="$(adb shell date +%F 2>/dev/null | tr -d '\r')"

getc(){ awk -F, -v d="$1" 'NR>1&&$1==d{print $NF; f=1; exit} END{if(!f) print 0}'; }
cnt_raw(){ awk -F, -v d="$1" 'NR>1{ if(substr($1,1,10)==d) c++ } END{ print c+0 }'; }

BEF_D="$(adb exec-out run-as "$PKG" cat "$CSV_DAILY" 2>/dev/null | tr -d '\r' | getc "$TODAY")"
BEF_R="$(adb exec-out run-as "$PKG"  cat "$CSV_RAW"   2>/dev/null | tr -d '\r' | cnt_raw "$TODAY")"

TS1="$TODAY 09:11:07"
TS2="$TODAY 12:22:09"
adb exec-out run-as "$PKG" sh -c '
set -eu
f="'"$CSV_RAW"'"
printf "%s,UNLOCK\n%s,UNLOCK\n" "'"$TS1"'" "'"$TS2"'" >>"$f"
' >/dev/null

adb shell logcat -c >/dev/null 2>&1 || true
adb shell am broadcast -a "$ACT" -n "$RCV" -p "$PKG" >/dev/null 2>&1 || fail "(broadcast failed)"
sleep 1
adb shell logcat -d > /tmp/_unlocks_log.txt 2>/dev/null || true
cat /tmp/_unlocks_log.txt | tee "$LOG" >/dev/null

grep -q "TriggerReceiver.*ACTION_RUN_UNLOCK_ROLLUP" "$LOG" || fail "(receiver not observed)"
grep -q "UnlockRollupWorker" "$LOG" || fail "(worker not observed)"
grep -q "WM-WorkerWrapper.*SUCCESS.*UnlockRollupWorker" "$LOG" || fail "(worker did not succeed)"

AFT_D="$(adb exec-out run-as "$PKG" cat "$CSV_DAILY" 2>/dev/null | tr -d '\r' | getc "$TODAY")"
AFT_R="$(adb exec-out run-as "$PKG"  cat "$CSV_RAW"   2>/dev/null | tr -d '\r' | cnt_raw "$TODAY")"

adb exec-out run-as "$PKG" sh -c '
set -eu
in="'"$CSV_RAW"'"; tmp="${in}.tmp.$$"; a="'"$TS1"'"; b="'"$TS2"'"
awk -F, -v a="$a" -v b="$b" '"'"'NR==1{print;next}{ if($1!=a && $1!=b) print }'"'"' "$in" >"$tmp" && mv "$tmp" "$in"
' >/dev/null || true

INC=$(( ${AFT_R:-0} - ${BEF_R:-0} ))
echo "$AFT_D" | grep -Eq '^[0-9]+$' || fail "(daily not integer)"
[ "$INC" -ge 2 ] || fail "(raw increment < 2)"
[ "$AFT_D" -eq "$AFT_R" ] || fail "(daily != raw for today)"

echo "AT-1 RESULT=PASS" | tee "$OUT"
exit 0
