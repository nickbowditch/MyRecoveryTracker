#!/bin/sh
set -eu
OUT="evidence/v6.0/app_usage_by_category/tc6.1.txt"

[ -f .gitignore ] || : > .gitignore

for pat in '!evidence/' '!evidence/v6.0/' '!evidence/v6.0/app_usage_by_category/' '!evidence/v6.0/app_usage_by_category/*.txt'; do
  grep -Fxq "$pat" .gitignore || echo "$pat" >> .gitignore
done

git add .gitignore >/dev/null 2>&1 || true
git commit -m "gitignore: whitelist evidence/; add App Usage By Category TC-6 v6.0.1" >/dev/null 2>&1 || true

git check-ignore -q evidence/v6.0/app_usage_by_category/_probe.txt && {
  echo "TC-6 RESULT=FAIL (.gitignore still ignores evidence/app_usage_by_category)" | tee "$OUT"
  exit 1
}

echo "TC-6 RESULT=PASS" | tee "$OUT"
exit 0
