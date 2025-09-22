#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/unlocks/tc2.25.txt"
CSV_DAILY="files/daily_unlocks.csv"
CSV_RAW="files/unlock_log.csv"
ACT="$PKG.ACTION_RUN_UNLOCK_ROLLUP"
CMP="$PKG/.TriggerReceiver"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "TC-2 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG"  >/dev/null 2>&1 || { echo "TC-2 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

T="$(adb shell date +%F | tr -d '\r')"
adb exec-out run-as "$PKG" sh -c '
mkdir -p files
[ -f "'"$CSV_RAW"'" ]   || printf "ts,event\n" >"'"$CSV_RAW"'"
[ -f "'"$CSV_DAILY"'" ] || printf "date,feature_schema_version,daily_unlocks\n" >"'"$CSV_DAILY"'"
' >/dev/null

getd(){ awk -F, -v d="$1" 'NR>1&&$1==d{print $NF;f=1;exit} END{if(!f)print 0}'; }
countr(){ awk -F, -v d="$1" 'NR>1{if(substr($1,1,10)==d)c++} END{print c+0}'; }

before_daily="$(adb exec-out run-as "$PKG" cat "$CSV_DAILY" | tr -d '\r' | getd "$T")"
before_raw="$(adb exec-out run-as "$PKG"  cat "$CSV_RAW"   | tr -d '\r' | countr "$T")"

TS1="$T 00:00:01"; TS2="$T 00:00:03"
adb exec-out run-as "$PKG" sh -c '
f="'"$CSV_RAW"'"; a="'"$TS1"'"; b="'"$TS2"'"
printf "%s,UNLOCK\n%s,UNLOCK\n" "$a" "$b" >> "$f"
' >/dev/null

deadline=$(( $(date +%s) + 30 ))
ok=1
while :; do
  adb shell am broadcast -a "$ACT" -n "$CMP" >/dev/null 2>&1 || true
  sleep 2
  after_daily="$(adb exec-out run-as "$PKG" cat "$CSV_DAILY" | tr -d '\r' | getd "$T")"
  after_raw="$(adb exec-out run-as "$PKG"  cat "$CSV_RAW"   | tr -d '\r' | countr "$T")"
  inc_raw=$(( ${after_raw:-0} - ${before_raw:-0} ))
  if [ "${after_daily:-0}" -eq "${after_raw:-0}" ] && [ "$inc_raw" -ge 2 ]; then ok=0; break; fi
  [ "$(date +%s)" -ge "$deadline" ] && break
done

adb exec-out run-as "$PKG" sh -c '
in="'"$CSV_RAW"'"; tmp="${in}.tmp.$$"; a="'"$TS1"'"; b="'"$TS2"'"
awk -F, -v a="$a" -v b="$b" '"'"'NR==1{print;next}{if($1!=a && $1!=b) print}'"'"' "$in" > "$tmp" && mv "$tmp" "$in"
' >/dev/null || true

[ $ok -eq 0 ] && { echo "TC-2 RESULT=PASS" | tee "$OUT"; exit 0; } \
              || { echo "TC-2 RESULT=FAIL (daily=$(adb exec-out run-as "$PKG" cat "$CSV_DAILY" | tr -d '\r' | getd "$T") raw=$(adb exec-out run-as "$PKG" cat "$CSV_RAW" | tr -d '\r' | countr "$T"))" | tee "$OUT"; exit 1; }
