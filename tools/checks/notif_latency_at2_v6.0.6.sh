#!/bin/sh
set -eu
APP="com.nick.myrecoverytracker"
RAW="files/notification_latency_log.csv"
DAILY="files/daily_notification_latency.csv"
OUT="evidence/v6.0/notification_latency/at2.txt"
TOL=50

fail(){ echo "AT2 RESULT=FAIL $1" | tee "$OUT"; exit 1; }

adb get-state >/dev/null 2>&1 || fail "(no device)"
adb shell pm path "$APP" >/dev/null 2>&1 || fail "(app not installed)"

RAW_DATA="$(adb exec-out run-as "$APP" cat "$RAW" 2>/dev/null | tr -d '\r' || true)"
DAILY_DATA="$(adb exec-out run-as "$APP" cat "$DAILY" 2>/dev/null | tr -d '\r' || true)"
[ -n "$RAW_DATA" ] || fail "(missing raw)"
[ -n "$DAILY_DATA" ] || fail "(missing daily)"

dates="$(printf '%s\n' "$DAILY_DATA" | awk -F, 'NR>1 && $1 ~ /^[0-9]{4}-[0-9]{2}-[0-9]{2}$/ {print $1}' | sort -u)"
[ -n "$dates" ] || fail "(no dates in daily)"

pct_from_sorted() {
  p="$1"
  awk -v p="$p" 'BEGIN{n=0} /^[0-9]+$/ {a[++n]=$1} END{
    if(n==0){print ""; exit}
    k = int((p*n + 99)/100); if(k<1)k=1; if(k>n)k=n
    print a[k]
  }'
}

compare_date() {
  d="$1"
  dd="$(printf '%s\n' "$DAILY_DATA" | awk -F, -v d="$d" 'NR>1 && $1==d {print $3","$4","$5","$6; exit}')"
  [ -n "$dd" ] || { echo "MISS $d (no daily row)"; return; }
  dd_p50="$(printf '%s' "$dd" | cut -d, -f1)"
  dd_p90="$(printf '%s' "$dd" | cut -d, -f2)"
  dd_p99="$(printf '%s' "$dd" | cut -d, -f3)"
  dd_cnt="$(printf '%s' "$dd" | cut -d, -f4)"

  lat_list="$(printf '%s\n' "$RAW_DATA" | awk -F, -v d="$d" '
    NR==1{for(i=1;i<=NF;i++){if($i=="posted_ts"||$i=="ts") pti=i; if($i=="latency_ms"||$i=="latency") li=i} next}
    {pt=$pti; lm=$li; if(length(pt)>=10 && substr(pt,1,10)==d){ if(lm ~ /^[0-9]+$/) print lm}}
  ')"
  n="$(printf '%s\n' "$lat_list" | grep -Ec '^[0-9]+$' || true)"
  n="${n:-0}"

  if [ "$n" -eq 0 ]; then
    dd_cnt="${dd_cnt:-0}"
    [ "$dd_cnt" = "0" ] || { echo "BAD $d cnt_raw=0 cnt_daily=$dd_cnt"; return; }
    ok_p50=0; ok_p90=0; ok_p99=0
    [ -z "${dd_p50:-}" ] || [ "${dd_p50:-}" = "0" ] || ok_p50=1
    [ -z "${dd_p90:-}" ] || [ "${dd_p90:-}" = "0" ] || ok_p90=1
    [ -z "${dd_p99:-}" ] || [ "${dd_p99:-}" = "0" ] || ok_p99=1
    if [ $ok_p50 -eq 0 ] && [ $ok_p90 -eq 0 ] && [ $ok_p99 -eq 0 ]; then
      echo "OK  $d cnt=0 (percentiles blank/0)"
    else
      echo "BAD $d cnt=0 (percentiles not blank/0)"
    fi
    return
  fi

  srt="$(printf '%s\n' "$lat_list" | grep -E '^[0-9]+$' | sort -n)"
  rp50="$(printf '%s\n' "$srt" | pct_from_sorted 50)"
  rp90="$(printf '%s\n' "$srt" | pct_from_sorted 90)"
  rp99="$(printf '%s\n' "$srt" | pct_from_sorted 99)"

  printf '%s\n' "${dd_p50:-}" | grep -Eq '^[0-9]+$' || { echo "BAD $d p50 non-numeric"; return; }
  printf '%s\n' "${dd_p90:-}" | grep -Eq '^[0-9]+$' || { echo "BAD $d p90 non-numeric"; return; }
  printf '%s\n' "${dd_p99:-}" | grep -Eq '^[0-9]+$' || { echo "BAD $d p99 non-numeric"; return; }
  printf '%s\n' "${dd_cnt:-}" | grep -Eq '^[0-9]+$' || { echo "BAD $d count non-numeric"; return; }

  diff() { a="$1"; b="$2"; x=$((a-b)); [ $x -lt 0 ] && x=$(( -x )); echo "$x"; }

  d50="$(diff "$rp50" "$dd_p50")"
  d90="$(diff "$rp90" "$dd_p90")"
  d99="$(diff "$rp99" "$dd_p99")"

  if [ "$d50" -le "$TOL" ] && [ "$d90" -le "$TOL" ] && [ "$d99" -le "$TOL" ] && [ "$dd_cnt" -eq "$n" ]; then
    echo "OK  $d p50=$dd_p50~$rp50 p90=$dd_p90~$rp90 p99=$dd_p99~$rp99 n=$n"
  else
    echo "BAD $d p50=$dd_p50~$rp50(d=$d50) p90=$dd_p90~$rp90(d=$d90) p99=$dd_p99~$rp99(d=$d99) daily_n=$dd_cnt raw_n=$n"
  fi
}

RES=""
while IFS= read -r d; do
  [ -n "$d" ] || continue
  RES="${RES}$(compare_date "$d")\n"
done <<EOF_D
$dates
EOF_D

printf "%b" "$RES" | tee "$OUT" >/dev/null
printf "%b" "$RES" | grep -q '^BAD ' && { echo "AT2 RESULT=FAIL" | tee -a "$OUT"; exit 1; }
echo "AT2 RESULT=PASS" | tee -a "$OUT"
exit 0
