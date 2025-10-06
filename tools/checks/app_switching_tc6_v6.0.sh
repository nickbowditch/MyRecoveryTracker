#!/bin/sh
set -eu
OUT="evidence/v6.0/app_switching/tc6.txt"

[ -f .gitignore ] || : > .gitignore

for pat in '!evidence/' '!evidence/v6.0/' '!evidence/v6.0/app_switching/' '!evidence/v6.0/app_switching/*.txt'; do
grep -Fxq "$pat" .gitignore || echo "$pat" >> .gitignore
done

git add .gitignore >/dev/null 2>&1 || true
git commit -m "gitignore: whitelist evidence/; add App Switching TC-6 v6.0" >/dev/null 2>&1 || true

if git check-ignore -q evidence/v6.0/app_switching/_probe.txt; then
  echo "TC-6 RESULT=FAIL (.gitignore still ignores evidence/app_switching)" | tee "$OUT"
  echo "--- DEBUG ---" | tee -a "$OUT"
  grep evidence .gitignore | tee -a "$OUT"
  exit 1
fi

echo "TC-6 RESULT=PASS" | tee "$OUT"
exit 0
