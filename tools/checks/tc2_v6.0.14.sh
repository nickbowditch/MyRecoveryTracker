#!/bin/bash
PKG="com.nick.myrecoverytracker"

adb get-state >/dev/null 2>&1 || { echo "TC-2 RESULT=FAIL (no device)"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "TC-2 RESULT=FAIL (app not installed)"; exit 3; }

EPOCH="$(adb shell date +%s | tr -d $'\r')"
D_TODAY="$(adb shell date +%F | tr -d $'\r')"
D_YEST="$(adb shell toybox date -d "@$((EPOCH-86400))" +%F | tr -d $'\r')"

get_count_col() {
  head_line="$1"
  awk -F, -v h="$head_line" 'BEGIN{
    n=split(h,a,",");
    for(i=1;i<=n;i++) if(a[i]=="daily_unlocks"){print i; exit}
    if(h=="date,unlocks"){print 2; exit}
    print 2
  }'
}

get_cnt() {
  d="$1"; c="$2"; csv="$3"
  awk -F, -v d="$d" -v c="$2" 'NR>1 && $1==d {print $c; f=1; exit} END{if(!f)print 0}' <<<"$csv"
}

CSV="$(adb exec-out run-as "$PKG" cat files/daily_unlocks.csv 2>/dev/null || printf "")"
HEAD="$(printf '%s\n' "$CSV" | head -n1 | tr -d $'\r')"
CCOL="$(get_count_col "$HEAD")"

C_Y_BEFORE="$(get_cnt "$D_YEST" "$CCOL" "$CSV")"
C_T_BEFORE="$(get_cnt "$D_TODAY" "$CCOL" "$CSV")"

adb exec-out run-as "$PKG" sh -c "
mkdir -p files
[ -f files/unlock_log.csv ] || printf 'ts,event\n' > files/unlock_log.csv
printf '%s,UNLOCK\n%s,UNLOCK\n%s,UNLOCK\n%s,UNLOCK\n' \
'$D_YEST 23:59:57' '$D_YEST 23:59:59' '$D_TODAY 00:00:01' '$D_TODAY 00:00:03' >> files/unlock_log.csv
"

for i in 1 2; do
  adb shell am broadcast -a "$PKG".ACTION_RUN_UNLOCK_ROLLUP -n "$PKG"/.TriggerReceiver >/dev/null
  sleep 2
done

CSV2="$(adb exec-out run-as "$PKG" cat files/daily_unlocks.csv 2>/dev/null || printf "")"
HEAD2="$(printf '%s\n' "$CSV2" | head -n1 | tr -d $'\r')"
CCOL2="$(get_count_col "$HEAD2")"

C_Y_AFTER="$(get_cnt "$D_YEST" "$CCOL2" "$CSV2")"
C_T_AFTER="$(get_cnt "$D_TODAY" "$CCOL2" "$CSV2")"

DY=$((C_Y_AFTER - C_Y_BEFORE))
DT=$((C_T_AFTER - C_T_BEFORE))

echo "TC-2 EXPECT +2 EACH — Y:$D_YEST before=$C_Y_BEFORE after=$C_Y_AFTER delta=$DY ; T:$D_TODAY before=$C_T_BEFORE after=$C_T_AFTER delta=$DT"

if [ "$DY" -eq 2 ] && [ "$DT" -eq 2 ]; then
  echo "TC-2 RESULT=PASS"
  exit 0
else
  echo "TC-2 RESULT=FAIL"
  printf "%s\n" "$CSV2" | tail -n 20
  exit 1
fi
