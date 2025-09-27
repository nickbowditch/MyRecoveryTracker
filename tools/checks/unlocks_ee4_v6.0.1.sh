#!/bin/sh
set -eu

OUT="evidence/v6.0/unlocks/ee4.txt"
mkdir -p "$(dirname "$OUT")"

git rev-parse --is-inside-work-tree >/dev/null 2>&1 || { echo "EE-4 RESULT: FAIL (not a git repo)" | tee "$OUT"; exit 2; }

[ -f .gitignore ] || : > .gitignore

append_if_missing() { p="$1"; grep -Fqx "$p" .gitignore || printf "%s\n" "$p" >> .gitignore; }
append_if_missing '!evidence'
append_if_missing '!evidence/v6.0'
append_if_missing '!evidence/v6.0/unlocks'
append_if_missing '!evidence/v6.0/unlocks/*.txt'

git add .gitignore >/dev/null 2>&1 || true
git commit -m "whitelist evidence/v6.0/unlocks/*.txt (EE-4 v6.0.1)" >/dev/null 2>&1 || true
git rm -r --cached evidence >/dev/null 2>&1 || true

touch evidence/v6.0/unlocks/_probe.txt
if git add evidence/v6.0/unlocks/_probe.txt >/dev/null 2>&1; then
  echo "EE-4 RESULT: PASS" | tee "$OUT"
  exit 0
fi

echo "EE-4 RESULT: FAIL (probe not addable)" | tee "$OUT"
exit 1
