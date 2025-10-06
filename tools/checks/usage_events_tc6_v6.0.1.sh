#!/bin/sh
set -eu
OUT="evidence/v6.0/usage_events_daily/tc6.1.txt"

[ -f .gitignore ] || : > .gitignore

for pat in '!evidence/' '!evidence/v6.0/' '!evidence/v6.0/usage_events_daily/' '!evidence/v6.0/usage_events_daily/*.txt'; do
grep -Fxq "$pat" .gitignore || echo "$pat" >> .gitignore
done

git add .gitignore >/dev/null 2>&1 || true
git commit -m "gitignore: whitelist evidence/; add Usage Events Daily TC-6 v6.0.1" >/dev/null 2>&1 || true

git check-ignore -q evidence/v6.0/usage_events_daily/_probe.txt && {
echo "TC-6 RESULT=FAIL (.gitignore still ignores evidence/usage_events_daily)" | tee "$OUT"
exit 1
}

echo "TC-6 RESULT=PASS" | tee "$OUT"
exit 0
