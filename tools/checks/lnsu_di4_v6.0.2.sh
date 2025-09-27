#!/bin/sh
set -eu
OUT="evidence/v6.0/lnsu/di4.txt"
S=0

git rev-parse --is-inside-work-tree >/dev/null 2>&1 || { echo "DI-4 RESULT=FAIL (not a git repo)" | tee "$OUT"; exit 2; }

[ -f .gitignore ] || : > .gitignore
for l in '!evidence/' '!evidence/v6.0/' '!evidence/v6.0/lnsu/' '!evidence/v6.0/lnsu/.txt'; do
grep -Fxq "$l" .gitignore || printf "%s\n" "$l" >> .gitignore
done

git add .gitignore >/dev/null 2>&1 || true
if [ -d evidence/v6.0/lnsu ]; then
find evidence/v6.0/lnsu -type f -name '.txt' -print0 2>/dev/null | xargs -0 git add -f >/dev/null 2>&1 || true
fi

git commit -m "gitignore: whitelist evidence/v6.0/lnsu/*.txt (lnsu DI-4 v6.0.2)" >/dev/null 2>&1 || true

git check-ignore -q evidence/v6.0/lnsu/_probe.txt 2>/dev/null && S=1

[ "$S" -eq 0 ] && { echo "DI-4 RESULT=PASS" | tee "$OUT"; exit 0; }
echo "DI-4 RESULT=FAIL (.gitignore still ignores evidence/lnsu)" | tee "$OUT"; exit 1
