#!/bin/bash
set -eu
OUT="evidence/v6.0/notification_latency/gv5.1.txt"
mkdir -p "$(dirname "$OUT")"
git rev-parse --is-inside-work-tree >/dev/null 2>&1 || { echo "GV-5 RESULT=FAIL (not a git repo)" | tee "$OUT"; exit 2; }
[ -f .gitignore ] || : > .gitignore
need=("!evidence/" "!evidence/v6.0/" "!evidence/v6.0/notification_latency/" "!evidence/v6.0/notification_latency/*.txt")
for l in "${need[@]}"; do grep -Fxq "$l" .gitignore || printf "%s\n" "$l" >> .gitignore; done
git add .gitignore >/dev/null 2>&1 || true
git commit -m "gitignore: whitelist evidence/v6.0/notification_latency/**" >/dev/null 2>&1 || true
git check-ignore -q evidence/v6.0/notification_latency/_probe.txt 2>/dev/null && { echo "GV-5 RESULT=FAIL (.gitignore ignores evidence)" | tee "$OUT"; exit 1; }
echo "GV-5 RESULT=PASS" | tee "$OUT"; exit 0
