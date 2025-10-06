#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
RCV="$PKG/.TriggerReceiver"
ACT="com.nick.myrecoverytracker.ACTION_RUN_LNS_ROLLUP"
CSV="files/daily_late_night_screen_usage.csv"
LOGF="files/screen_log.csv"
UNL="files/unlock_log.csv"
OUT_DIR="evidence/v6.0/lnsu"
OUT="$OUT_DIR/at1.txt"
LOG="$OUT_DIR/at1.log.txt"
mkdir -p "$OUT_DIR"

fail(){ echo "AT1 RESULT=FAIL $1" | tee "$OUT"; exit 1; }

adb get-state >/dev/null 2>&1 || fail "(no device)"
adb shell pm path "$PKG" >/dev/null 2>&1 || fail "(app not installed)"

YDAY="$(python3 - <<'PY'
import datetime as dt
print((dt.datetime.now()-dt.timedelta(days=1)).strftime("%Y-%m-%d"))
PY
)"

adb exec-out run-as "$PKG" sh -c '
set -eu
mkdir -p files
[ -f "'"$LOGF"'" ] || printf "ts,event\n" >"'"$LOGF"'"
[ -f "'"$UNL"'" ]  || printf "ts,event\n" >"'"$UNL"'"
[ -f "'"$CSV"'" ]  || printf "date,feature_schema_version,is_late_night\n" >"'"$CSV"'"
' >/dev/null

adb exec-out run-as "$PKG" sh -c '
set -eu
sf="'"$LOGF"'"; uf="'"$UNL"'"; d="'"$YDAY"'"
printf "%s,ON\n%s,OFF\n" "$d 01:00:00" "$d 01:05:00" >> "$sf"
printf "%s,UNLOCK\n" "$d 01:02:00" >> "$uf"
' >/dev/null

adb shell logcat -c >/dev/null 2>&1 || true
adb shell cmd activity broadcast -n "$RCV" -a "$ACT" --receiver-foreground --user 0 >/dev/null 2>&1 || fail "(broadcast failed)"
sleep 1
adb shell logcat -d > /tmp/_lnsu_log.txt 2>/dev/null || true
cat /tmp/_lnsu_log.txt | tee "$LOG" >/dev/null

grep -q "TriggerReceiver.*ACTION_RUN_LNS_ROLLUP" "$LOG" || fail "(receiver not observed)"
grep -q "LateNightScreenRollupWorker" "$LOG" || fail "(worker not observed)"
grep -q "WM-WorkerWrapper.*SUCCESS.*LateNightScreenRollupWorker" "$LOG" || fail "(worker did not succeed)"

adb exec-out run-as "$PKG" cat "$CSV" 2>/dev/null > "$OUT_DIR/dlnsu.after.csv" || :
ROW="$(grep -F "$YDAY," "$OUT_DIR/dlnsu.after.csv" | tail -n1 || true)"
case "$ROW" in
  *",Y") echo "AT1 RESULT=PASS" | tee "$OUT"; exit 0 ;;
  *)     fail "($YDAY not Y)" ;;
esac
