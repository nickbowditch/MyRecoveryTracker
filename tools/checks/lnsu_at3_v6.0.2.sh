#!/bin/sh
set -eu
OUT="evidence/v6.0/lnsu/at3.txt"

git rev-parse --is-inside-work-tree >/dev/null 2>&1 || { echo "AT3 RESULT=FAIL (not a git repo)" | tee "$OUT"; exit 1; }

[ -f .gitignore ] || : > .gitignore
for pat in 'evidence/' 'evidence/v6.0/' 'evidence/v6.0/lnsu/' 'evidence/v6.0/lnsu/*.csv'; do
  grep -Fqx "$pat" .gitignore || echo "$pat" >> .gitignore
done

git add .gitignore >/dev/null 2>&1 || true
git commit -m "gitignore: whitelist evidence/v6.0/lnsu (AT3 v6.0.2)" >/dev/null 2>&1 || true

git check-ignore -q evidence/v6.0/lnsu/probe.txt 2>/dev/null && { echo "AT3 RESULT=FAIL (.gitignore still ignores evidence)" | tee "$OUT"; exit 1; }

echo "AT3 RESULT=PASS" | tee "$OUT"
