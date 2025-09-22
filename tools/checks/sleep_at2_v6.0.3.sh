#!/bin/sh
PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/sleep/at2.3.txt"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "AT-2 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "AT-2 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

CSV_A="$(adb exec-out run-as "$PKG" sh -c 'cat files/daily_sleep.csv 2>/dev/null' | tr -d '\r')"
CSV_B="$(adb exec-out run-as "$PKG" sh -c 'cat files/daily_sleep_duration.csv 2>/dev/null' | tr -d '\r')"
[ -n "$CSV_A" ] && [ -n "$CSV_B" ] || { echo "AT-2 RESULT=FAIL (missing csv)" | tee "$OUT"; exit 4; }

awk -F',' '
function abs(x){return x<0?-x:x}
function isint(x){return x ~ /^-?[0-9]+$/}
function isnum(x){return x ~ /^-?[0-9]+([.][0-9]+)?$/}

NR==FNR{
  if(FNR==1) {
    for(i=1;i<=NF;i++){ if($i=="date") di=i; if($i=="duration_hours") hi=i; if($i=="duration_minutes") mi=i }
    next
  }
  d=$di; v=""
  if(hi && $hi!=""){ v=$hi; from="hr" }
  else if(mi && $mi!=""){ v=$mi; from="min" }
  if(from=="hr" && isnum(v)) h[d]=v+0.0
  else if(from=="min" && isint(v)) h[d]=(v+0)/60.0
  next
}
FNR==1{
  for(i=1;i<=NF;i++){ if($i=="date") d2=i; if($i=="hours") h2=i }
  next
}
FNR>1{
  d=$d2; v=$h2
  if(isnum(v)) g[d]=v+0.0
}
END{
  bad=0
  # check both ways
  for(d in h){
    a=h[d]+0.0; b=(d in g)? g[d]+0.0 : 0.0
    if(abs(a-b)>0.05){ print d"," a "," b; bad=1 }
  }
  for(d in g){
    b=g[d]+0.0; a=(d in h)? h[d]+0.0 : 0.0
    if(abs(a-b)>0.05){ print d"," a "," b; bad=1 }
  }
  if(bad==0) print "AT-2 RESULT=PASS"; else print "AT-2 RESULT=FAIL"
}
' <(printf '%s\n' "$CSV_A") <(printf '%s\n' "$CSV_B") | {
  read first
  if [ "$first" = "AT-2 RESULT=PASS" ]; then
    echo "$first" | tee "$OUT" >/dev/null; exit 0
  else
    echo "$first" | tee "$OUT" >/dev/null
    sed -n '2,$p' | tee -a "$OUT" >/dev/null
    exit 1
  fi
}
