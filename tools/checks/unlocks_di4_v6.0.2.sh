#!/bin/sh
set -eu
OUT="evidence/v6.0/_repo/di4.2.txt"
mkdir -p "$(dirname "$OUT")"
git rev-parse --is-inside-work-tree >/dev/null 2>&1 || { echo "DI-4 RESULT=FAIL (not a git repo)" | tee "$OUT"; exit 2; }
[ -f .gitignore ] || : > .gitignore
for pat in '!evidence/' '!evidence/v6.0/' '!evidence/v6.0/*.txt'; do
grep -Fxq "$pat" .gitignore || echo "$pat" >> .gitignore
done
git add .gitignore >/dev/null 2>&1 || true
git commit -m "gitignore: whitelist evidence/** (unlocks DI-4 v6.0.2)" >/dev/null 2>&1 || true
git check-ignore -q evidence/v6.0/_probe.txt && { echo "DI-4 RESULT=FAIL (.gitignore still ignores evidence/)" | tee "$OUT"; exit 1; }
echo "DI-4 RESULT=PASS" | tee "$OUT"
exit 0
