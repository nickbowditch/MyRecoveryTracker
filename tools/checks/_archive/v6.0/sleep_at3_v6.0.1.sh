#!/bin/bash
OUT="evidence/v6.0/sleep/at3.1.txt"
git rev-parse --is-inside-work-tree >/dev/null 2>&1 || { echo "AT-3 RESULT=FAIL (not a git repo)" | tee "$OUT"; exit 2; }
[ -f .gitignore ] || : > .gitignore
need=("!evidence/" "!evidence/v6.0/" "!evidence/v6.0/sleep/" "!evidence/v6.0/sleep/*.txt")
for l in "${need[@]}"; do grep -Fxq "$l" .gitignore || printf "%s\n" "$l" >> .gitignore; done
git add .gitignore >/dev/null 2>&1 || true
git commit -m "gitignore: whitelist evidence/v6.0/sleep/**" >/dev/null 2>&1 || true
git check-ignore -q evidence/v6.0/sleep/_probe.txt 2>/dev/null
[ $? -eq 0 ] && { echo "AT-3 RESULT=FAIL (.gitignore still ignores evidence)" | tee "$OUT"; exit 1; }
echo "AT-3 RESULT=PASS" | tee "$OUT"; exit 0
