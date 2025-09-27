#!/bin/sh
set -eu

APP="${APP:-com.nick.myrecoverytracker}"
CSV="files/daily_lnslu.csv"
LOCK="app/locks/di.lnslu.lock"
OUT="evidence/v6.0/lnsu/gv6.txt"

mkdir -p "$(dirname "$OUT")"

[ -f "$LOCK" ] || { echo "GV6 RESULT=FAIL (missing $LOCK)" | tee "$OUT"; exit 1; }

if ! awk -F'=' '
BEGIN{f=0;m0=0;M240=0}
{gsub(/\r$/,""); k=$1; v=$2; gsub(/^[ \t]+|[ \t]+$/,"",k); gsub(/^[ \t]+|[ \t]+$/,"",v)}
k=="file" && v=="files/daily_lnslu.csv"{f=1}
k=="min" && v=="0"{m0=1}
k=="max" && v=="240"{M240=1}
END{exit !(f&&m0&&M240)}
' "$LOCK"; then
echo "GV6 RESULT=FAIL (lock mismatch)" | tee "$OUT"
exit 1
fi

adb shell run-as "$APP" test -f "$CSV" || { echo "GV6 RESULT=FAIL (missing $CSV on device)" | tee "$OUT"; exit 1; }

if adb exec-out run-as "$APP" cat "$CSV" 2>/dev/null | awk -F',' '
BEGIN{ok=1;hdr=0}
NR==1{a=$1;b=$2;gsub(/^[ \t]+|[ \t]+$/,"",a);gsub(/^[ \t]+|[ \t]+$/,"",b);sub(/\r$/,"",b);if(a!="date"||b!="minutes"){ok=0};hdr=1;next}
NR>1{v=$2;gsub(/^[ \t]+|[ \t]+$/,"",v);sub(/\r$/,"",v);if(v==""||v!~/^-?[0-9]+(\.[0-9]+)?$/){ok=0;exit}x=v+0;if(x<0||x>240){ok=0;exit}}
END{if(!hdr)ok=0;exit ok?0:1}
'; then
echo "GV6 RESULT=PASS" | tee "$OUT"; exit 0
fi

echo "GV6 RESULT=FAIL (bounds/header validation)" | tee "$OUT"
exit 1
