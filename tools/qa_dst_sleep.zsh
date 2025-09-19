#!/usr/bin/env zsh
PKG="${1:-com.nick.myrecoverytracker}"
TZ="${2:-Australia/Sydney}"
pull(){ adb exec-out run-as "$PKG" cat "files/$1" 2>/dev/null || echo ""; }
SUM="$(pull daily_sleep_summary.csv)"
python3 - "$TZ" "$SUM" <<'PY'
import sys,io,csv,datetime
from zoneinfo import ZoneInfo
tz=ZoneInfo(sys.argv[1]); txt=sys.argv[2]
def read(txt):
    m={}; dates=[]
    for i,row in enumerate(csv.reader(io.StringIO(txt))):
        if i==0 or not row or not row[0]: continue
        m[row[0]]=row; dates.append(datetime.date.fromisoformat(row[0]))
    dates.sort(); return m,dates
m,dates=read(txt)
if not dates: print("DST: INCONCLUSIVE no_data"); sys.exit(2)
def find_transition(ds):
    for i in range(len(ds)-1):
        d0,d1=ds[i],ds[i+1]
        if datetime.datetime(d0.year,d0.month,d0.day,12,tzinfo=tz).utcoffset()!=datetime.datetime(d1.year,d1.month,d1.day,12,tzinfo=tz).utcoffset():
            return d1
    return None
t=find_transition(dates)
if not t: print("DST: SKIP no_transition_in_data"); sys.exit(0)
trip=[t-datetime.timedelta(days=1),t,t+datetime.timedelta(days=1)]
miss=[d.isoformat() for d in trip if d.isoformat() not in m]
if miss: print("DST: INCONCLUSIVE missing_dates="+";".join(miss)); sys.exit(2)
def hms(s): h,m,s=map(int,s.split(":")); return h,m,s
bad=[]; tol=0.12
for d in trip:
    row=m[d.isoformat()]
    if len(row)<4 or not row[1] or not row[2] or not row[3]: bad.append((d,"summary_fields_missing")); continue
    sh,sm,ss=hms(row[1]); wh,wm,ws=hms(row[2]); dur=float(row[3])
    sdt=datetime.datetime.combine(d,datetime.time(sh,sm,ss,tzinfo=tz))
    wdt=datetime.datetime.combine(d,datetime.time(wh,wm,ws,tzinfo=tz))
    if wdt<=sdt: wdt+=datetime.timedelta(days=1)
    calc=(wdt-sdt).total_seconds()/3600.0
    if abs(calc-dur)>tol: bad.append((d,f"mismatch {calc:.2f}!={dur:.2f}"))
if bad:
    for d,why in bad: print("DST: FAIL", d.isoformat(), why)
    sys.exit(1)
print("DST: OK")
PY
