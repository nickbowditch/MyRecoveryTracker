#!/usr/bin/env zsh
TZ="Australia/Sydney"
python3 - <<'PY' "$TZ"
import datetime,sys
from zoneinfo import ZoneInfo
tz=ZoneInfo(sys.argv[1])
rows=[
    ["2025-10-04","23:30:00","07:30:00","8.00"],
    ["2025-10-05","23:30:00","07:30:00","8.00"],
    ["2025-10-06","23:30:00","07:30:00","8.00"],
]
def hms(s): h,m,s=map(int,s.split(":")); return h,m,s
bad=[]
for ds,sl,wa,dur in rows:
    d=datetime.date.fromisoformat(ds)
    sh,sm,ss=hms(sl); wh,wm,ws=hms(wa)
    sdt=datetime.datetime.combine(d,datetime.time(sh,sm,ss,tzinfo=tz))
    wdt=datetime.datetime.combine(d,datetime.time(wh,wm,ws,tzinfo=tz))
    if wdt<=sdt: wdt+=datetime.timedelta(days=1)
    dur_calc=(wdt-sdt).total_seconds()/3600.0
    if abs(dur_calc-float(dur))>0.11:
        bad.append((ds,f"mismatch calc={dur_calc:.2f} csv={float(dur):.2f}"))
if bad:
    for ds,why in bad: print("DST: FAIL", ds, why); sys.exit(1)
print("DST: OK (fixture)")
PY
