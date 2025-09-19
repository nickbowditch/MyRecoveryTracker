#!/bin/bash
PKG="com.nick.myrecoverytracker"

adb get-state >/dev/null 2>&1 || { echo "TC-2 RESULT=FAIL (no device)"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "TC-2 RESULT=FAIL (app not installed)"; exit 3; }

EPOCH="$(adb shell date +%s | tr -d $'\r')"
D_TODAY="$(adb shell date +%F | tr -d $'\r')"
D_YEST="$(adb shell toybox date -d "@$((EPOCH-86400))" +%F | tr -d $'\r')"

get_val_col() {
  h="$1"
  awk -F, -v H="$h" 'BEGIN{
    n=split(H,a,",");
    for(i=1;i<=n;i++) if(a[i]=="sleep_minutes" || a[i]=="daily_sleep"){print i; exit}
    if(H=="date,minutes"){print 2; exit}
    print 2
  }'
}

get_val() {
  d="$1"; c="$2"; csv="$3"
  awk -F, -v d="$d" -v c="$2" 'NR>1 && $1==d {print $c; f=1; exit} END{if(!f)print 0}' <<<"$csv"
}

CSV="$(adb exec-out run-as "$PKG" cat files/daily_sleep.csv 2>/dev/null || printf "")"
HEAD="$(printf '%s\n' "$CSV" | head -n1 | tr -d $'\r')"
VCOL="$(get_val_col "$HEAD")"

Y_BEFORE="$(get_val "$D_YEST" "$VCOL" "$CSV")"
T_BEFORE="$(get_val "$D_TODAY" "$VCOL" "$CSV")"

adb exec-out run-as "$PKG" sh -c "
mkdir -p files
[ -f files/sleep_log.csv ] || printf 'ts,event\n' > files/sleep_log.csv
printf '%s,SLEEP\n%s,SLEEP\n%s,SLEEP\n%s,SLEEP\n' \
'$D_YEST 23:59:57' '$D_YEST 23:59:59' '$D_TODAY 00:00:01' '$D_TODAY 00:00:03' >> files/sleep_log.csv
" >/dev/null 2>&1

for i in 1 2; do
  adb shell am broadcast -a "$PKG".ACTION_RUN_SLEEP_ROLLUP -n "$PKG"/.TriggerReceiver >/dev/null
  sleep 2
done

CSV2="$(adb exec-out run-as "$PKG" cat files/daily_sleep.csv 2>/dev/null || printf "")"
HEAD2="$(printf '%s\n' "$CSV2" | head -n1 | tr -d $'\r')"
VCOL2="$(get_val_col "$HEAD2")"

Y_AFTER="$(get_val "$D_YEST" "$VCOL2" "$CSV2")"
T_AFTER="$(get_val "$D_TODAY" "$VCOL2" "$CSV2")"

DY=$((Y_AFTER - Y_BEFORE))
DT=$((T_AFTER - T_BEFORE))

echo "TC-2 EXPECT +2 EACH — Y:$D_YEST before=$Y_BEFORE after=$Y_AFTER delta=$DY ; T:$D_TODAY before=$T_BEFORE after=$T_AFTER delta=$DT"

if [ "$DY" -ge 2 ] && [ "$DT" -ge 2 ]; then
  echo "TC-2 RESULT=PASS"; exit 0
else
  echo "TC-2 RESULT=FAIL"
  printf "%s\n" "$CSV2" | tail -n 20
  exit 1
fi
