#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/sleep/at2.4.txt"
RCV="$PKG/.TriggerReceiver"
ACT1="$PKG.ACTION_RUN_SLEEP_ROLLUP"
ACT2="$PKG.ACTION_RUN_ROLLUP_SLEEP"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "AT-2 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "AT-2 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

adb shell am broadcast -a "$ACT1" -n "$RCV" >/dev/null 2>&1 || true
adb shell am broadcast -a "$ACT2" -n "$RCV" >/dev/null 2>&1 || true
sleep 2

SUM="$(adb exec-out run-as "$PKG" cat files/daily_sleep_summary.csv 2>/dev/null | tr -d '\r' || true)"
DUR="$(adb exec-out run-as "$PKG" cat files/daily_sleep_duration.csv 2>/dev/null | tr -d '\r' || true)"

[ -n "$SUM" ] || { echo "AT-2 RESULT=FAIL (missing daily_sleep_summary.csv)" | tee "$OUT"; exit 4; }
[ -n "$DUR" ] || { echo "AT-2 RESULT=FAIL (missing daily_sleep_duration.csv)" | tee "$OUT"; exit 5; }

if printf '%s\n' "$SUM" | awk 'NR>1{exit 1} END{exit 0}'; then
echo "AT-2 RESULT=PASS (no rows yet in summary)" | tee "$OUT"; exit 0
fi
if printf '%s\n' "$DUR" | awk 'NR>1{exit 1} END{exit 0}'; then
echo "AT-2 RESULT=PASS (no rows yet in duration)" | tee "$OUT"; exit 0
fi

to_minutes() {
awk -F',' '
NR==1{
for(i=1;i<=NF;i++){h[i]=$i; gsub(/[ \t]/,"",h[i]); lc[i]=tolower(h[i])}
dm=0; dh=0
for(i=1;i<=NF;i++){
if(lc[i]=="duration_minutes" || lc[i]=="minutes") dm=i
if(lc[i]=="duration_hours"   || lc[i]=="hours")   dh=i
if(lc[i]=="date") dcol=i
}
next
}
NR>1{
if(!dcol) next
d=$dcol
if(dm) m=$dm+0
else if(dh) m=int(($dh+0)*60+0.5)
else next
printf "%s,%d\n", d, int(m+0.5)
}'
}

S_MIN="$(printf '%s\n' "$SUM" | to_minutes | sort)"
D_MIN="$(printf '%s\n' "$DUR" | to_minutes | sort)"

if awk -F',' '
FNR==NR { a[$1]=$2+0; seen[$1]=1; next }
{ b[$1]=$2+0; seen[$1]=1 }
END {
bad=0
for (k in seen) {
da = (k in a)?a[k]:0
db = (k in b)?b[k]:0
diff = da - db
if (diff < -1 || diff > 1) { bad=1; break }
}
exit bad
}' \
<(printf '%s\n' "$S_MIN") <(printf '%s\n' "$D_MIN"); then
echo "AT-2 RESULT=PASS" | tee "$OUT"; exit 0
else
echo "AT-2 RESULT=FAIL" | tee "$OUT"; exit 1
fi
