#!/bin/sh
set -eu
APP="${APP:-com.nick.myrecoverytracker}"
RCV="$APP/.TriggerReceiver"
ACT="com.nick.myrecoverytracker.ACTION_RUN_LNS_ROLLUP"
OUT="evidence/v6.0/lnsu/at1.txt"

YDAY=$(python3 - <<'PY'
import datetime as dt; print((dt.datetime.now()-dt.timedelta(days=1)).strftime("%Y-%m-%d"))
PY
)

mkdir -p evidence/v6.0/lnsu
adb exec-out run-as "$APP" cat files/daily_late_night_screen_usage.csv 2>/dev/null > evidence/v6.0/lnsu/dlnsu.before.csv || :

adb shell run-as "$APP" sh <<EOS
set -eu
sf=files/screen_log.csv
uf=files/unlock_log.csv
[ -f "\$sf" ] || exit 4
[ -f "\$uf" ] || : > "\$uf"
printf "%s,ON\n%s,OFF\n" "$YDAY 01:00:00" "$YDAY 01:05:00" >> "\$sf"
printf "%s,UNLOCK\n" "$YDAY 01:02:00" >> "\$uf"
EOS

adb shell cmd activity broadcast -n "$RCV" -a "$ACT" --receiver-foreground --user 0 >/dev/null
sleep 1
adb exec-out run-as "$APP" cat files/daily_late_night_screen_usage.csv > evidence/v6.0/lnsu/dlnsu.after.csv

row=$(grep -F "$YDAY," evidence/v6.0/lnsu/dlnsu.after.csv | tail -n1 || true)
case "$row" in
*",Y") echo "AT1 RESULT=PASS" | tee "$OUT"; exit 0 ;;
*)     echo "AT1 RESULT=FAIL ($YDAY not Y)" | tee "$OUT"; exit 1 ;;
esac
