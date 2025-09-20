#!/bin/sh
OUT="evidence/v6.0/sleep/gv4.15.txt"
mkdir -p "$(dirname "$OUT")"
FILES="$(git ls-files -- 'evidence/v6.0/sleep/*.txt' 2>/dev/null || true)"
[ -n "$FILES" ] || { echo "GV-4 RESULT=FAIL (no tracked numbered logs)" | tee "$OUT"; exit 3; }
LATEST="$(printf '%s\n' "$FILES" | awk -F/ '
{
  bn=$NF
  n=split(bn,a,".")
  if(n==3 && a[3]=="txt"){
    key=a[1]; ver=a[2]
    if(ver+0 >= best[key]+0){ best[key]=ver; path[key]=$0 }
  }
}
END{ for(k in path) print path[k] }')"
bad=0; det=""
for f in $LATEST; do
  [ -z "$f" ] && continue
  grep -q 'RESULT=PASS' "$f" || { bad=1; det="${det}RED:$f\n"; }
done
if [ "$bad" -eq 0 ]; then echo "GV-4 RESULT=PASS" | tee "$OUT"; exit 0; else { echo "GV-4 RESULT=FAIL"; printf "%b" "$det"; } | tee "$OUT"; exit 1; fi
