#!/bin/sh
set -eu
OUT="evidence/v6.0/lnsu/at3.txt"

git rev-parse --is-inside-work-tree >/dev/null 2>&1 || { echo "AT3 RESULT=FAIL (not a git repo)" | tee "$OUT"; exit 1; }

[ -f .gitignore ] || : > .gitignore

append_if_missing() {
  pat="$1"
  grep -Fqx "$pat" .gitignore || printf "%s\n" "$pat" >> .gitignore
}

append_if_missing '!evidence/'
append_if_missing '!evidence/v6.0/'
append_if_missing '!evidence/v6.0/lnsu/'
append_if_missing '!evidence/v6.0/lnsu/*.csv'
append_if_missing '!evidence/v6.0/lnsu/*.txt'

git add .gitignore >/dev/null 2>&1 || true
git commit -m "gitignore: whitelist evidence/v6.0/lnsu (AT3 v6.0.3)" >/dev/null 2>&1 || true

if git check-ignore -q evidence/v6.0/lnsu/probe.txt 2>/dev/null; then
  echo "AT3 RESULT=FAIL (.gitignore still ignores evidence)" | tee "$OUT"
  exit 1
fi

echo "AT3 RESULT=PASS" | tee "$OUT"
