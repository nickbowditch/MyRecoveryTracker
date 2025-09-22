#!/bin/sh
OUT="evidence/v6.0/sleep/gv3.12.txt"
mkdir -p "$(dirname "$OUT")"
set -- tools/checks/sleep__v6.0..sh
[ -e "$1" ] || { echo "GV-3 RESULT=FAIL (no versioned sleep checks)" | tee "$OUT"; exit 2; }
for f in "$@"; do
bn="$(basename "$f")"
case "$bn" in sleep_gv*) continue ;; esac
base="${bn%.sh}"
key="${base#sleep_}"; key="${key%%v6.0.}"
ver="${base##v6.0.}"
eval "cur=\${best$key:-}"
if [ -z "$cur" ] || [ "$ver" -ge "$cur" ]; then
eval "best$key=$ver"
fi
done
miss=0; det=""
for kv in $(set | grep '^best_' | cut -d= -f1); do
key="${kv#best_}"
ver="$(eval "printf %s \"\${$kv}\"")"
ev="evidence/v6.0/sleep/${key}.${ver}.txt"
[ -s "$ev" ] || { miss=1; det="${det}MISSING:${ev}\n"; continue; }
grep -q 'RESULT=PASS' "$ev" || { miss=1; det="${det}RED:${ev}\n"; }
done
if [ "$miss" -eq 0 ]; then echo "GV-3 RESULT=PASS" | tee "$OUT"; exit 0; else { echo "GV-3 RESULT=FAIL"; printf "%b" "$det"; } | tee "$OUT"; exit 1; fi
