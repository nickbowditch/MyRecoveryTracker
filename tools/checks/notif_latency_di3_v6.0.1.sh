#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
CSV="files/daily_notification_latency.csv"
OUT_DIR="evidence/v6.0/notification_latency"
OUT="$OUT_DIR/di3.1.txt"
mkdir -p "$OUT_DIR"

DEV="$(adb devices 2>/dev/null | awk 'NR>1 && $2=="device"{print $1; exit}')"
[ -n "${DEV:-}" ] || { echo "DI-3 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb -s "$DEV" shell pm path "$PKG" >/dev/null 2>&1 || { echo "DI-3 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

BAD="$(adb -s "$DEV" exec-out run-as "$PKG" awk -F, '
function trim(s){gsub(/^[[:space:]]+|[[:space:]]+$/,"",s); return s}
function parse_ms(s, allow_blank,   t,n){
t=trim(s)
if(t==""){ if(allow_blank){ms_ok=1; return 0} ms_ok=0; return 0 }
if(t ~ /^[0-9]+$/){ n=t+0; if(n>=0 && n<=3600000){ ms_ok=1; return n } }
ms_ok=0; return 0
}
BEGIN{bad=0}
NR==1{
for(i=1;i<=NF;i++){
h[i]=$i
if($i=="count") c=i
if($i=="p50_ms") p50=i
if($i=="p90_ms") p90=i
if($i=="p99_ms") p99=i
}
if(!(c&&p50&&p90&&p99)){
if(NF>=6){ c=3; p50=4; p90=5; p99=6 }
}
if(!(c&&p50&&p90&&p99)){
print "HEADER_BAD"
exit
}
next
}
{
cnts=trim($c)
if(cnts !~ /^[0-9]+$/){ print "line " NR ": non-integer count :: " $0; bad++; next }
cnt=cnts+0
if(cnt<0 || cnt>5000){ print "line " NR ": count out of bounds :: " $0; bad++; next }

allow=(cnt==0?1:0)
v50=parse_ms($p50, allow); ok50=ms_ok
v90=parse_ms($p90, allow); ok90=ms_ok
v99=parse_ms($p99, allow); ok99=ms_ok

if(!ok50){ print "line " NR ": p50_ms invalid :: " $0; bad++; next }
if(!ok90){ print "line " NR ": p90_ms invalid :: " $0; bad++; next }
if(!ok99){ print "line " NR ": p99_ms invalid :: " $0; bad++; next }

if(!(v50<=v90 && v90<=v99)){ print "line " NR ": percentile order violated :: " $0; bad++; next }
}
END{
if(bad>0){ exit 1 }
}
' "$CSV" 2>/dev/null || true)"

case "$BAD" in
"HEADER_BAD") echo "DI-3 RESULT=FAIL (bad header: missing count/p50_ms/p90_ms/p99_ms)"; echo "$BAD" | sed -n '2,$p' >> "$OUT"; exit 5 ;;
esac

if [ -z "$BAD" ]; then
echo "DI-3 RESULT=PASS" | tee "$OUT"
exit 0
else
echo "DI-3 RESULT=FAIL" | tee "$OUT"
printf '%s\n' "$BAD" >> "$OUT"
exit 6
fi
