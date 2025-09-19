#!/usr/bin/env zsh
PKG="${1:-com.nick.myrecoverytracker}"
pull(){ adb exec-out run-as "$PKG" cat "files/$1" 2>/dev/null || echo ""; }
SUM="$(pull daily_sleep_summary.csv)"
DUR="$(pull daily_sleep_duration.csv)"
ST="$(pull daily_sleep_time.csv)"
WT="$(pull daily_wake_time.csv)"
python3 - "$SUM" "$DUR" "$ST" "$WT" <<'PY'
import sys,io,csv,datetime
tol=0.12
def m(txt):
    r={}
    for i,row in enumerate(csv.reader(io.StringIO(txt))):
        if i==0 or not row or not row[0]: continue
        r[row[0]]=row
    return r
sumr, durr, strr, wtr = m(sys.argv[1]), m(sys.argv[2]), m(sys.argv[3]), m(sys.argv[4])
today=datetime.date.today()
last=[(today-datetime.timedelta(days=i)).isoformat() for i in range(14)]
present=[d for d in last if d in sumr]
if not present:
    print("CONSISTENCY: INCONCLUSIVE no_recent_rows"); sys.exit(2)

def hms(s): h,m,s=map(int,s.split(":")); return h,m,s
bad=[]
for d in present:
    s=sumr[d]
    if len(s) < 4: bad.append((d,"summary_fields_missing")); continue
    st, wt, dh = (s[1] or "").strip(), (s[2] or "").strip(), (s[3] or "").strip()
    if dh in ("0","0.0","0.00") and st=="" and wt=="":
        continue
    if not st or not wt or not dh:
        bad.append((d,"summary_fields_missing")); continue
    try:
        sh,sm,ss=hms(st); wh,wm,ws=hms(wt); dur_sum=float(dh)
    except Exception:
        bad.append((d,"parse_error")); continue
    base=datetime.date.fromisoformat(d)
    sdt=datetime.datetime.combine(base,datetime.time(sh,sm,ss))
    wdt=datetime.datetime.combine(base,datetime.time(wh,wm,ws))
    if wdt<=sdt: wdt+=datetime.timedelta(days=1)
    calc=(wdt-sdt).total_seconds()/3600.0
    if abs(calc-dur_sum)>tol: bad.append((d,f"sum_vs_calc {dur_sum:.2f}!={calc:.2f}"))
    if d in durr and len(durr[d])>=2 and durr[d][1]:
        try:
            dur_file=float(durr[d][1])
            if abs(dur_file-dur_sum)>tol: bad.append((d,f"sum_vs_duration_csv {dur_sum:.2f}!={dur_file:.2f}"))
        except: bad.append((d,"bad_duration_csv_value"))
future=[k for k in sumr if k>today.isoformat()]
if future: bad.append(("future_dates",",".join(sorted(future)[:5])))
if bad:
    for ds,why in bad: print("CONSISTENCY: FAIL", ds, why)
    sys.exit(1)
print("CONSISTENCY: OK")
PY
