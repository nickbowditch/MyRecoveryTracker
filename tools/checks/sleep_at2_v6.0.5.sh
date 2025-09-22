#!/bin/sh
PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/sleep/at2.5.txt"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { printf '%s\n' "AT-2 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { printf '%s\n' "AT-2 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

CSV_A="$(adb exec-out run-as "$PKG" cat files/daily_sleep.csv 2>/dev/null | tr -d '\r')"
CSV_B="$(adb exec-out run-as "$PKG" cat files/daily_sleep_duration.csv 2>/dev/null | tr -d '\r')"
[ -n "$CSV_A" ] && [ -n "$CSV_B" ] || { printf '%s\n' "AT-2 RESULT=FAIL (missing csv)" | tee "$OUT"; exit 4; }

A_FILE="$(mktemp)"; B_FILE="$(mktemp)"
trap 'rm -f "$A_FILE" "$B_FILE"' EXIT INT TERM
printf '%s\n' "$CSV_A" > "$A_FILE"
printf '%s\n' "$CSV_B" > "$B_FILE"

RES="$(awk -F',' '
function abs(x){return x<0?-x:x}
function isint(x){return x ~ /^-?[0-9]+$/}
function isnum(x){return x ~ /^-?[0-9]+([.][0-9]+)?$/}

NR==FNR{
  if(FNR==1){
    for(i=1;i<=NF;i++){ if($i=="date") di=i; if($i=="duration_hours") hi=i; if($i=="duration_minutes") mi=i }
    next
  }
  d=$di; v=""; from=""
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
  for(d in h){
    a=h[d]; b=(d in g)?g[d]:0
    if(abs(a-b)>0.05){ print "MISMATCH," d "," a "," b; bad=1 }
  }
  for(d in g){
    b=g[d]; a=(d in h)?h[d]:0
    if(abs(a-b)>0.05){ print "MISMATCH," d "," a "," b; bad=1 }
  }
  if(bad==0) print "AT-2 RESULT=PASS"; else print "AT-2 RESULT=FAIL"
}
' "$A_FILE" "$B_FILE")"

printf '%s\n' "$RES" | tee "$OUT" >/dev/null

printf '%s\n' "$RES" | grep -q "RESULT=PASS"
