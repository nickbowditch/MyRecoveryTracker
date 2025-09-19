#!/bin/sh
set -e
PKG=com.nick.myrecoverytracker

# install .sleep_rollup.sh
adb shell "run-as $PKG /system/bin/sh -s" <<'EOS'
cat > files/.sleep_rollup.sh <<'SLEEP'
#!/system/bin/sh
d=$(date +%F)
u=files/unlock_log.csv
s=files/screen_log.csv

mk(){ f="$1"; h="$2"; [ -f "$f" ] || echo "$h" > "$f"; }
mk files/daily_sleep.csv            "date,sleep_time,wake_time,duration_hours"
mk files/daily_sleep_summary.csv    "date,sleep_time,wake_time,duration_hours"
mk files/daily_sleep_duration.csv   "date,hours"
mk files/daily_sleep_time.csv       "date,HH:MM:SS"
mk files/daily_wake_time.csv        "date,HH:MM:SS"

[ -f "$u" ] || exit 0
[ -f "$s" ] || exit 0

wts=$(awk -v d="$d" '
function tosec(h,m,s){return h*3600+m*60+s}
function ts(line,    hh,mm,ss,p,ev){
  if (substr(line,1,10)!=d) return -1
  if (substr(line,5,1)!="-"||substr(line,8,1)!="-"||substr(line,14,1)!=":"||substr(line,17,1)!=":") return -1
  hh=substr(line,12,2)+0; mm=substr(line,15,2)+0; ss=substr(line,18,2)+0
  p=index(line,","); ev=(p?toupper(substr(line,p+1)):"")
  if (ev ~ /^UNLOCK|^USER_PRESENT|^UNLOCKED/) return tosec(hh,mm,ss)
  return -1
}
$0 ~ "^" d {
  t=ts($0)
  if (t>=14400 && t<=43200) { if (w==0 || t<w) w=t }
}
END{ if (w>0) print w }' "$u")

[ -n "$wts" ] || wts=0
if [ "$wts" -le 0 ]; then
  awk -F, -v d="$d" 'NR==1{H=$0;next}$1!=d{print}END{print H;print d ",,," 0}' files/daily_sleep_summary.csv > files/.tmp && mv files/.tmp files/daily_sleep_summary.csv
  awk -F, -v d="$d" 'NR==1{H=$0;next}$1!=d{print}END{print H;print d "," 0}' files/daily_sleep_duration.csv > files/.tmp && mv files/.tmp files/daily_sleep_duration.csv
  awk -F, -v d="$d" 'NR==1{H=$0;next}$1!=d{print}END{print H;print d "," ""}' files/daily_sleep_time.csv > files/.tmp && mv files/.tmp files/daily_sleep_time.csv
  awk -F, -v d="$d" 'NR==1{H=$0;next}$1!=d{print}END{print H;print d "," ""}' files/daily_wake_time.csv > files/.tmp && mv files/.tmp files/daily_wake_time.csv
  awk -F, -v d="$d" 'NR==1{H=$0;next}$1!=d{print}END{print H;print d ",,," 0}' files/daily_sleep.csv > files/.tmp && mv files/.tmp files/daily_sleep.csv
  exit 0
fi

awk -v d="$d" -v W="$wts" '
function tosec(h,m,s){return h*3600+m*60+s}
function parse(line,    day,hh,mm,ss,p,ev,ts,rel){
  day=substr(line,1,10)
  if (substr(line,5,1)!="-"||substr(line,8,1)!="-"||substr(line,14,1)!=":"||substr(line,17,1)!=":") return
  hh=substr(line,12,2)+0; mm=substr(line,15,2)+0; ss=substr(line,18,2)+0
  p=index(line,","); ev=(p?toupper(substr(line,p+1)):"")
  ts=tosec(hh,mm,ss)
  if (day==d) rel=ts
  else if (day<d) rel=ts-86400
  else return
  if (index(ev,"SCREEN_OFF")==1 || ev=="OFF" || ev=="SCREEN_OFF_ACTION") {
    if (rel < W) {
      gap = W - rel
      if (gap >= 600 && gap <= 50400) {
        if (best_rel=="" || rel > best_rel) {
          best_rel=rel; best_day=day; best_ts=sprintf("%02d:%02d:%02d",hh,mm,ss); best_gap=gap
        }
      }
    }
  }
}
{ parse($0) }
END{
  if (best_rel=="") { print "NO"; exit }
  printf "OK,%s,%s,%.2f\n", best_day, best_ts, (best_gap/3600.0)
}' "$s" | while IFS=, read status off_day off_hms hours; do
  if [ "$status" != "OK" ]; then
    awk -F, -v d="$d" 'NR==1{H=$0;next}$1!=d{print}END{print H;print d ",,," 0}' files/daily_sleep_summary.csv > files/.tmp && mv files/.tmp files/daily_sleep_summary.csv
    awk -F, -v d="$d" 'NR==1{H=$0;next}$1!=d{print}END{print H;print d "," 0}' files/daily_sleep_duration.csv > files/.tmp && mv files/.tmp files/daily_sleep_duration.csv
    awk -F, -v d="$d" 'NR==1{H=$0;next}$1!=d{print}END{print H;print d "," ""}' files/daily_sleep_time.csv > files/.tmp && mv files/.tmp files/daily_sleep_time.csv
    awk -v t="$wts" 'function clock(t){h=int(t/3600);m=int((t%3600)/60);s=int(t%60);print sprintf("%02d:%02d:%02d",h,m,s)} END{print clock(t)}' /dev/null > files/.wake.tmp
    wake_hms=$(cat files/.wake.tmp); rm -f files/.wake.tmp
    awk -F, -v d="$d" -v w="$wake_hms" 'NR==1{H=$0;next}$1!=d{print}END{print H;print d "," w}' files/daily_wake_time.csv > files/.tmp && mv files/.tmp files/daily_wake_time.csv
    awk -F, -v d="$d" 'NR==1{H=$0;next}$1!=d{print}END{print H;print d ",,," 0}' files/daily_sleep.csv > files/.tmp && mv files/.tmp files/daily_sleep.csv
    exit 0
  fi

  awk -v t="$wts" 'function clock(t){h=int(t/3600);m=int((t%3600)/60);s=int(t%60);print sprintf("%02d:%02d:%02d",h,m,s)} END{print clock(t)}' /dev/null > files/.wake.tmp
  wake_hms=$(cat files/.wake.tmp); rm -f files/.wake.tmp

  awk -F, -v d="$d" -v s="$off_hms" -v w="$wake_hms" -v h="$hours" 'NR==1{H=$0;next}$1!=d{print}END{print H;print d "," s "," w "," h}' files/daily_sleep_summary.csv > files/.tmp && mv files/.tmp files/daily_sleep_summary.csv
  awk -F, -v d="$d" -v h="$hours" 'NR==1{H=$0;next}$1!=d{print}END{print H;print d "," h}' files/daily_sleep_duration.csv > files/.tmp && mv files/.tmp files/daily_sleep_duration.csv
  awk -F, -v d="$d" -v s="$off_hms" 'NR==1{H=$0;next}$1!=d{print}END{print H;print d "," s}' files/daily_sleep_time.csv > files/.tmp && mv files/.tmp files/daily_sleep_time.csv
  awk -F, -v d="$d" -v w="$wake_hms" 'NR==1{H=$0;next}$1!=d{print}END{print H;print d "," w}' files/daily_wake_time.csv > files/.tmp && mv files/.tmp files/daily_wake_time.csv
  awk -F, -v d="$d" -v s="$off_hms" -v w="$wake_hms" -v h="$hours" 'NR==1{H=$0;next}$1!=d{print}END{print H;print d "," s "," w "," h}' files/daily_sleep.csv > files/.tmp && mv files/.tmp files/daily_sleep.csv
done
SLEEP
chmod 700 files/.sleep_rollup.sh
EOS

# install .late_night_screen_rollup.sh
adb shell "run-as $PKG /system/bin/sh -s" <<'EOS'
cat > files/.late_night_screen_rollup.sh <<'LATE'
#!/system/bin/sh
d=$(date +%F)
[ -f files/daily_late_night_screen_usage.csv ] || echo "date,late_night_YN" > files/daily_late_night_screen_usage.csv
flag="N"
for f in files/screen_log.csv files/unlock_log.csv; do
  [ -f "$f" ] || continue
  c=$(awk -v d="$d" '
    function is_today(){return substr($0,1,10)==d}
    function hhmmss(){return substr($0,12,8)}
    function t2s(x){return substr(x,1,2)*3600+substr(x,4,2)*60+substr(x,7,2)}
    is_today(){ if (is_today()){ s=t2s(hhmmss()); if (s>=0 && s<18000) n++ } }
    END{print n+0}
  ' "$f")
  [ "$c" -gt 0 ] && { flag="Y"; break; }
done
awk -F, -v d="$d" -v f="$flag" 'NR==1{H=$0;next}$1==d{next}{print}END{print H;print d "," f}' files/daily_late_night_screen_usage.csv > files/.tmp && mv files/.tmp files/daily_late_night_screen_usage.csv
LATE
chmod 700 files/.late_night_screen_rollup.sh
EOS

# run now and show tails
adb shell run-as "$PKG" /system/bin/sh -c '
files/.sleep_rollup.sh; files/.late_night_screen_rollup.sh
for f in files/daily_sleep.csv files/daily_sleep_summary.csv files/daily_sleep_duration.csv files/daily_sleep_time.csv files/daily_wake_time.csv files/daily_late_night_screen_usage.csv; do
  echo "-- $f"; tail -n 3 "$f" 2>/dev/null || true; echo
done
'
