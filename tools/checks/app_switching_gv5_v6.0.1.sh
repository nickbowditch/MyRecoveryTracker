#!/bin/sh
set -eu
OUT="evidence/v6.0/app_switching/gv5.1.txt"
mkdir -p "$(dirname "$OUT")"

git rev-parse --is-inside-work-tree >/dev/null 2>&1 || { echo "GV-5 RESULT=FAIL (not a git repo)" | tee "$OUT"; exit 2; }

[ -f .gitignore ] || : > .gitignore
add_allow() { p="$1"; grep -Fqx "$p" .gitignore || printf "%s\n" "$p" >> .gitignore; }
add_allow '!evidence/'
add_allow '!evidence/v6.0/'
add_allow '!evidence/v6.0/app_switching/'
add_allow '!evidence/v6.0/app_switching/*.txt'

git add .gitignore >/dev/null 2>&1 || true
git commit -m "gitignore: whitelist evidence/v6.0/app_switching/*.txt (GV-5 v6.0.1)" >/dev/null 2>&1 || true

PROBE="evidence/v6.0/app_switching/_probe.txt"
echo "probe $(date +%s)" > "$PROBE"

if git add "$PROBE" >/dev/null 2>&1; then
  if git check-ignore -q "$PROBE" 2>/dev/null; then
    echo "GV-5 RESULT=FAIL (.gitignore still ignores probe)" | tee "$OUT"
    exit 1
  fi
  echo "GV-5 RESULT=PASS" | tee "$OUT"
  exit 0
fi

echo "GV-5 RESULT=FAIL (git add failed)" | tee "$OUT"
exit 1
