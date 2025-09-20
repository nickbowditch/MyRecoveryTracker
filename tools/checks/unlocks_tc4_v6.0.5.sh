#!/bin/sh
set -eu

PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/unlocks/tc4.5.txt"
CSV_DAILY="files/daily_unlocks.csv"
CSV_RAW="files/unlock_log.csv"
ROLLUP_ACTION="$PKG.ACTION_RUN_UNLOCK_ROLLUP"
ROLLUP_COMPONENT="$PKG/.TriggerReceiver"

mkdir -p "$(dirname "$OUT")"
: > "$OUT"
say(){ printf '%s\n' "$*" | tee -a "$OUT"; }

adb get-state >/dev/null 2>&1 || { say "TC-4 RESULT=FAIL (no device)"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { say "TC-4 RESULT=FAIL (app not installed)"; exit 3; }

adb exec-out run-as "$PKG" sh -c '
mkdir -p files
[ -f "'"$CSV_RAW"'" ]   || printf "ts,event\n" > "'"$CSV_RAW"'"
[ -f "'"$CSV_DAILY"'" ] || printf "date,feature_schema_version,daily_unlocks\n" > "'"$CSV_DAILY"'"
' >/dev/null

DAILY_BEFORE="$(adb exec-out run-as "$PKG" cat "$CSV_DAILY" 2>/dev/null | tr -d '\r' || true)"
RAW_BEFORE="$(adb exec-out run-as "$PKG" cat "$CSV_RAW" 2>/dev/null | tr -d '\r' || true)"

get_daily(){ awk -F, -v dd="$1" 'NR>1&&$1==dd{print $3;f=1;exit} END{if(!f)print 0}'; }
count_raw_for(){ awk -F, -v dd="$1" 'NR>1{d=substr($1,1,10); if(d==dd)c++} END{print c+0}'; }

pick_dates(){
  start_off=1; try=0
  while [ $try -lt 20 ]; do
    d1="$(date -u -v+${start_off}d +%F 2>/dev/null || date -u -d "+${start_off} days" +%F)"
    d2="$(date -u -v+$((start_off+1))d +%F 2>/dev/null || date -u -d "+$((start_off+1)) days" +%F)"
    in1="$(printf '%s\n' "$DAILY_BEFORE" | awk -F, -v dd="$d1" 'NR>1&&$1==dd{print 1;exit}')"
    in2="$(printf '%s\n' "$DAILY_BEFORE" | awk -F, -v dd="$d2" 'NR>1&&$1==dd{print 1;exit}')"
    r1="$(printf '%s\n' "$RAW_BEFORE"   | awk -F, -v dd="$d1" 'NR>1{if(substr($1,1,10)==dd){print 1;exit}}')"
    r2="$(printf '%s\n' "$RAW_BEFORE"   | awk -F, -v dd="$d2" 'NR>1{if(substr($1,1,10)==dd){print 1;exit}}')"
    [ -z "$in1$in2$r1$r2" ] && { echo "$d1 $d2"; return 0; }
    start_off=$((start_off+2)); try=$((try+1))
  done
  return 1
}

dates="$(pick_dates)" || { say "TC-4 RESULT=FAIL (no free dates)"; exit 4; }
D1="${dates%% *}"; D2="${dates##* }"

D1_BEFORE="$(printf '%s\n' "$DAILY_BEFORE" | get_daily "$D1")"
D2_BEFORE="$(printf '%s\n' "$DAILY_BEFORE" | get_daily "$D2")"

adb exec-out run-as "$PKG" sh -c '
f="'"$CSV_RAW"'"
printf "%s,UNLOCK\n%s,UNLOCK\n%s,UNLOCK\n" "'"$D1"' 09:00:00" "'"$D1"' 12:00:00" "'"$D1"' 18:00:00" >> "$f"
printf "%s,UNLOCK\n%s,UNLOCK\n%s,UNLOCK\n" "'"$D2"' 09:00:00" "'"$D2"' 12:00:00" "'"$D2"' 18:00:00" >> "$f"
' >/dev/null

i=0; while [ $i -lt 2 ]; do
  adb shell am broadcast -a "$ROLLUP_ACTION" -n "$ROLLUP_COMPONENT" >/dev/null 2>&1 || true
  sleep 2
  i=$((i+1))
done

DAILY_AFTER="$(adb exec-out run-as "$PKG" cat "$CSV_DAILY" 2>/dev/null | tr -d '\r' || true)"
RAW_AFTER="$(adb exec-out run-as "$PKG" cat "$CSV_RAW" 2>/dev/null | tr -d '\r' || true)"

D1_DAILY="$(printf '%s\n' "$DAILY_AFTER" | get_daily "$D1")"
D2_DAILY="$(printf '%s\n' "$DAILY_AFTER" | get_daily "$D2")"
D1_RAW="$(printf '%s\n' "$RAW_AFTER"   | count_raw_for "$D1")"
D2_RAW="$(printf '%s\n' "$RAW_AFTER"   | count_raw_for "$D2")"

ok=0
if [ "$D1_DAILY" = "$D1_RAW" ] && [ "$D2_DAILY" = "$D2_RAW" ]; then
  if [ $((D1_DAILY - D1_BEFORE)) -eq 3 ] && [ $((D2_DAILY - D2_BEFORE)) -eq 3 ]; then ok=1; fi
fi

adb exec-out run-as "$PKG" sh -c '
in="'"$CSV_RAW"'"; tmp="${in}.tmp.$$"
awk -F, -v d1="'"$D1"'" -v d2="'"$D2"'" '"'"'
NR==1{print $0; next}
{ dd=substr($1,1,10); if(dd!=d1 && dd!=d2) print $0 }
'"'"' "$in" > "$tmp" && mv "$tmp" "$in"
' >/dev/null || true

if [ $ok -eq 1 ]; then
  say "TC-4 RESULT=PASS"
  exit 0
else
  say "TC-4 RESULT=FAIL"
  say "$D1 delta=$((D1_DAILY - D1_BEFORE)) daily=$D1_DAILY raw=$D1_RAW"
  say "$D2 delta=$((D2_DAILY - D2_BEFORE)) daily=$D2_DAILY raw=$D2_RAW"
  exit 1
fi
