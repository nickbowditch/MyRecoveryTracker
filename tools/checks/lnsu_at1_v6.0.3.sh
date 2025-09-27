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
f=files/screen_log.csv
[ -f "\$f" ] || exit 4
printf "%s,ON\n%s,OFF\n" "$YDAY 01:00:00" "$YDAY 01:05:00" >> "\$f"
EOS

adb shell cmd activity broadcast -n "$RCV" -a "$ACT" --receiver-foreground --user 0 >/dev/null
adb exec-out run-as "$APP" cat files/daily_late_night_screen_usage.csv > evidence/v6.0/lnsu/dlnsu.after.csv

b=$(shasum -a 1 evidence/v6.0/lnsu/dlnsu.before.csv 2>/dev/null | awk '{print $1}')
a=$(shasum -a 1 evidence/v6.0/lnsu/dlnsu.after.csv | awk '{print $1}')

if [ "${b:-none}" != "$a" ]; then
  echo "AT1 RESULT=PASS" | tee "$OUT"
  exit 0
fi
echo "AT1 RESULT=FAIL (no change detected)" | tee "$OUT"
exit 1
