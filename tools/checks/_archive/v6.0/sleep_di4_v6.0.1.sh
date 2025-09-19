#!/bin/bash
OUT="evidence/v6.0/sleep/di4.1.txt"
S=0

git rev-parse --is-inside-work-tree >/dev/null 2>&1 || { echo "DI-4 RESULT=FAIL (not a git repo)" | tee "$OUT"; exit 2; }

[ -f .gitignore ] || : > .gitignore
need=("!evidence/" "!evidence/v6.0/" "!evidence/v6.0/sleep/" "!evidence/v6.0/sleep/*.txt")

for l in "${need[@]}"; do
  grep -Fxq "$l" .gitignore || printf "%s\n" "$l" >> .gitignore
done

git add .gitignore >/dev/null 2>&1 || true
if [ -d evidence/v6.0/sleep ]; then
  find evidence/v6.0/sleep -type f -name '*.txt' -print0 2>/dev/null | xargs -0 git add -f >/dev/null 2>&1 || true
fi
git commit -m "gitignore: whitelist evidence/v6.0/sleep/**; ensure logs are tracked" >/dev/null 2>&1 || true

git check-ignore -q evidence/v6.0/sleep/_probe.txt 2>/dev/null
[ $? -eq 0 ] && S=1

if [ "$S" -eq 0 ]; then
  echo "DI-4 RESULT=PASS" | tee "$OUT"
  exit 0
else
  echo "DI-4 RESULT=FAIL (.gitignore still ignores evidence)" | tee "$OUT"
  exit 1
fi
