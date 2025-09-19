#!/usr/bin/env zsh
PKG="${1:-com.nick.myrecoverytracker}"
pull(){ adb exec-out run-as "$PKG" cat "files/$1" 2>/dev/null || echo ""; }
SUM="$(pull daily_sleep_summary.csv)"
DUR="$(pull daily_sleep_duration.csv)"
ST="$(pull daily_sleep_time.csv)"
WT="$(pull daily_wake_time.csv)"
python3 - "$SUM" "$DUR" "$ST" "$WT" <<'PY' | adb shell run-as "$PKG" sh -c 'mkdir files >/dev/null 2>&1 || true; cat > files/daily_sleep_summary.csv'
import sys,io,csv,datetime
sum_txt,dur_txt,st_txt,wt_txt = sys.argv[1:5]
def load(txt):
    m={}
    for i,row in enumerate(csv.reader(io.StringIO(txt))):
        if i==0 or not row or not row[0]: continue
        m[row[0]]=row[1:]
    return m
sumr=load(sum_txt); durr=load(dur_txt); strr=load(st_txt); wtr=load(wt_txt)
dates=sorted(set(sumr)|set(durr)|set(strr)|set(wtr))
out=[["date","sleep_time","wake_time","duration_hours"]]
def calc_hours(s,w):
    try:
        sh,sm,ss=map(int,s.split(":")); wh,wm,ws=map(int,w.split(":"))
    except: return ""
    sd=datetime.datetime(2000,1,1,sh,sm,ss)
    wd=datetime.datetime(2000,1,1,wh,wm,ws)
    if wd<=sd: wd+=datetime.timedelta(days=1)
    return f"{(wd-sd).total_seconds()/3600.0:.2f}"
for d in dates:
    s_time = (sumr.get(d,[None,None,None])[0] if len(sumr.get(d,[]))>=1 else None) or (strr.get(d,[None])[0] if d in strr else "")
    w_time = (sumr.get(d,[None,None,None])[1] if len(sumr.get(d,[]))>=2 else None) or (wtr.get(d,[None])[0] if d in wtr else "")
    dur    = (sumr.get(d,[None,None,None])[2] if len(sumr.get(d,[]))>=3 else None) or (durr.get(d,[None])[0] if d in durr else "")
    if (not dur or dur=="") and s_time and w_time:
        dur = calc_hours(s_time,w_time)
    out.append([d, s_time or "", w_time or "", dur or ""])
w=io.StringIO(); csv.writer(w, lineterminator="\n").writerows(out); print(w.getvalue(), end="")
PY
