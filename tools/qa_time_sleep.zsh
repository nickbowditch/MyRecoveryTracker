#!/usr/bin/env zsh
PKG="${1:-com.nick.myrecoverytracker}"

pull() { adb exec-out run-as "$PKG" cat "files/$1" 2>/dev/null || echo "__MISSING__"; }

DUR="$(pull daily_sleep_duration.csv)"
SUM="$(pull daily_sleep_summary.csv)"
STIME="$(pull daily_sleep_time.csv)"
WTIME="$(pull daily_wake_time.csv)"

S=0

echo "$DUR" | awk -F, 'NR>1&&$1!=""{d[$1]++; if($2==""||$2<0||$2>24){bad=1}}
END{ for(k in d) if(d[k]>1){print "FAIL: duplicate dates in duration.csv"; exit 1}
     if(bad){print "FAIL: duration out of range"; exit 1} }' || S=1

echo "$DUR" | awk -F, 'NR>1&&$1!=""{printf "\"%s\"\n",$1}' | tail -n 14 | \
python3 - <<'PY' || S=1
import sys, datetime
rows = [l.strip().strip('"') for l in sys.stdin if l.strip()]
ok = True
try:
    dates = sorted(set(datetime.date.fromisoformat(r) for r in rows))
    for i in range(1, len(dates)):
        if (dates[i] - dates[i-1]).days > 2:
            print(f"FAIL: gap over 2 days between {dates[i-1]} and {dates[i]}")
            ok = False
    if ok:
        print("OK: midnight gaps check")
except Exception as e:
    print(f"FAIL: date parse error -> {e}")
    ok = False
if not ok:
    sys.exit(1)
PY

chk_time_window () {
  local CSV="$1" WHAT="$2" LOW="$3" HIGH="$4"
  echo "$CSV" | awk -F, -v L="$LOW" -v H="$HIGH" -v W="$WHAT" '
    function tosec(t){ split(t,a,":"); return a[1]*3600+a[2]*60+a[3] }
    BEGIN{ l=tosec(L); h=tosec(H); tol=3600; cross=(l>h) }
    NR>1 && $1!="" && $2!="" {
      if(!match($2,/^[0-2][0-9]:[0-5][0-9]:[0-5][0-9]$/)){print "FAIL: bad time format in",W; exit 1}
      s=tosec($2)
      ok = cross ? (s >= (l-tol) || s <= (h+tol)) : (s >= (l-tol) && s <= (h+tol))
      if(!ok){printf("WARN: %s %s out of window (%s)\n", W,$1,$2)}
    }'
}

[ -n "$WTIME" ] && chk_time_window "$WTIME" "wake" "04:00:00" "23:59:59" || true
[ -n "$STIME" ] && chk_time_window "$STIME" "sleep" "20:00:00" "04:00:00" || true

echo "$SUM" | head -n1 | grep -q '^date,' || { echo "FAIL: summary header"; S=1; }

[ $S -eq 0 ] && echo "PASS: sleep time-correctness" || echo "FAIL: sleep time-correctness"
exit $S
