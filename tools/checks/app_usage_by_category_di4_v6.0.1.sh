#!/bin/sh
set -eu
OUT="evidence/v6.0/app_usage_by_category/di4.txt"
mkdir -p "$(dirname "$OUT")"

git rev-parse --is-inside-work-tree >/dev/null 2>&1 || { echo "DI-4 RESULT=FAIL (not a git repo)" | tee "$OUT"; exit 2; }

[ -f .gitignore ] || : > .gitignore
for l in '!evidence/' '!evidence/v6.0/' '!evidence/v6.0/app_usage_by_category/' '!evidence/v6.0/app_usage_by_category/*.txt'; do
  grep -Fxq "$l" .gitignore || printf "%s\n" "$l" >> .gitignore
done

git add .gitignore >/dev/null 2>&1 || true
if [ -d evidence/v6.0/app_usage_by_category ]; then
  find evidence/v6.0/app_usage_by_category -type f -name '*.txt' -print0 2>/dev/null | xargs -0 git add -f >/dev/null 2>&1 || true
fi
git commit -m "gitignore: whitelist evidence/v6.0/app_usage_by_category/*.txt (app usage by category DI-4 v6.0.1)" >/dev/null 2>&1 || true

if git check-ignore -q evidence/v6.0/app_usage_by_category/_probe.txt 2>/dev/null; then
  echo "DI-4 RESULT=FAIL (.gitignore still ignores evidence/app_usage_by_category)" | tee "$OUT"
  exit 1
fi

echo "DI-4 RESULT=PASS" | tee "$OUT"
exit 0
