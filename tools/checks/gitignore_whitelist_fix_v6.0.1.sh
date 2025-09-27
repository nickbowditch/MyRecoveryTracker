#!/bin/sh
set -eu
OUT="evidence/v6.0/maintenance/gitignore_fix.1.txt"
mkdir -p "$(dirname "$OUT")"

git rev-parse --is-inside-work-tree >/dev/null 2>&1 || { echo "CLEANUP RESULT=FAIL (not a git repo)" | tee "$OUT"; exit 2; }

[ -f .gitignore ] || : > .gitignore
awk '/^evidence\/?$/ {next} /^\/?evidence\/?/ {next} {print}' .gitignore > .gitignore.tmp && mv .gitignore.tmp .gitignore

append() { l="$1"; grep -Fqx "$l" .gitignore || printf "%s\n" "$l" >> .gitignore; }
append '!evidence/'
append '!evidence/v6.0/'
append '!evidence/v6.0/unlocks/'
append '!evidence/v6.0/unlocks/*.txt'
append '!evidence/v6.0/lnsu/'
append '!evidence/v6.0/lnsu/*.txt'
append '!evidence/v6.0/sleep/'
append '!evidence/v6.0/sleep/*.txt'
append '!evidence/v6.0/notification_engagement/'
append '!evidence/v6.0/notification_engagement/*.txt'
append '!evidence/v6.0/maintenance/'
append '!evidence/v6.0/maintenance/*.txt'

git add .gitignore >/dev/null 2>&1 || true
git commit -m "Whitelist evidence/ portable rewrite" >/dev/null 2>&1 || true
git rm -r --cached evidence >/dev/null 2>&1 || true

probe() { p="$1/_probe.txt"; mkdir -p "$(dirname "$p")"; : > "$p"; git check-ignore -q "$p" 2>/dev/null; }
if probe evidence/v6.0/unlocks || probe evidence/v6.0/lnsu || probe evidence/v6.0/sleep || probe evidence/v6.0/notification_engagement || probe evidence/v6.0/maintenance; then
  echo "CLEANUP RESULT=FAIL (.gitignore still ignores evidence)" | tee "$OUT"; exit 1
else
  echo "CLEANUP RESULT=PASS" | tee "$OUT"; exit 0
fi
