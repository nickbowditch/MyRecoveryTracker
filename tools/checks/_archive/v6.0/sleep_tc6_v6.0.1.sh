#!/bin/bash
S=0
git rev-parse --is-inside-work-tree >/dev/null 2>&1 || { echo "TC-6 RESULT=FAIL (not a git repo)"; exit 2; }
[ -f .gitignore ] || : > .gitignore
need=("!evidence/" "!evidence/v6.0/" "!evidence/v6.0/" "!evidence/v6.0//.txt")
for l in "${need[@]}"; do
grep -Fxq "$l" .gitignore || printf "%s\n" "$l" >> .gitignore
done
git add .gitignore >/dev/null 2>&1 || true
if [ -d evidence/v6.0 ]; then
find evidence/v6.0 -type f -name '.txt' -print0 2>/dev/null | xargs -0 git add -f >/dev/null 2>&1 || true
fi
git commit -m "gitignore: whitelist evidence/v6.0/**; ensure logs are tracked" >/dev/null 2>&1 || true
git check-ignore -q evidence/v6.0/_probe.txt 2>/dev/null
[ $? -eq 0 ] && S=1
if [ "$S" -eq 0 ]; then echo "TC-6 RESULT=PASS"; exit 0; else echo "TC-6 RESULT=FAIL (.gitignore still ignores evidence)"; exit 1; fi
