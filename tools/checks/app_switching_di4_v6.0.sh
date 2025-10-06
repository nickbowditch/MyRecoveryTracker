#!/bin/sh
set -eu
OUT="evidence/v6.0/app_switching/di4.txt"
mkdir -p "$(dirname "$OUT")"

git rev-parse --is-inside-work-tree >/dev/null 2>&1 || { echo "DI-4 RESULT=FAIL (not a git repo)" | tee "$OUT"; exit 2; }

[ -f .gitignore ] || : > .gitignore
for l in '!evidence/' '!evidence/v6.0/' '!evidence/v6.0/app_switching/' '!evidence/v6.0/app_switching/*.txt'; do
grep -Fxq "$l" .gitignore || printf "%s\n" "$l" >> .gitignore
done

git add .gitignore >/dev/null 2>&1 || true
if [ -d evidence/v6.0/app_switching ]; then
find evidence/v6.0/app_switching -type f -name '*.txt' -print0 2>/dev/null | xargs -0 git add -f >/dev/null 2>&1 || true
fi
git commit -m "gitignore: whitelist evidence/v6.0/app_switching/*.txt (app switching DI-4 v6.0)" >/dev/null 2>&1 || true

if git check-ignore -q evidence/v6.0/app_switching/_probe.txt 2>/dev/null; then
echo "DI-4 RESULT=FAIL (.gitignore still ignores evidence/app_switching)" | tee "$OUT"
echo "--- DEBUG: .gitignore HEAD ---" | tee -a "$OUT"
head -n 20 .gitignore | tee -a "$OUT"
exit 1
fi

echo "DI-4 RESULT=PASS" | tee "$OUT"
exit 0
