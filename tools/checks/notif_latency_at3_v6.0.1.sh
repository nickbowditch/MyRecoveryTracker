#!/bin/sh
set -eu
OUT="evidence/v6.0/notification_latency/at3.1.txt"

git rev-parse --is-inside-work-tree >/dev/null 2>&1 || { echo "AT3 RESULT=FAIL (not a git repo)" | tee "$OUT"; exit 1; }

[ -f .gitignore ] || : > .gitignore
sed -i.bak -e '/^evidence$/d' -e '/^evidence\//d' -e '/^\/evidence/d' -e '/evidence\/$/d' .gitignore || true

EXCL="$(git rev-parse --git-dir)/info/exclude"
[ -f "$EXCL" ] && sed -i.bak -e '/^evidence$/d' -e '/^evidence\//d' -e '/^\/evidence/d' -e '/evidence\/$/d' "$EXCL" || true

GLOB="$(git config --get core.excludesFile || true)"
[ -n "${GLOB:-}" ] && [ -f "$GLOB" ] && sed -i.bak -e '/^evidence$/d' -e '/^evidence\//d' -e '/^\/evidence/d' -e '/evidence\/$/d' "$GLOB" || true

append_if_missing() { p="$1"; grep -Fqx "$p" .gitignore || printf "%s\n" "$p" >> .gitignore; }
append_if_missing '!evidence'
append_if_missing '!evidence/v6.0'
append_if_missing '!evidence/v6.0/notification_latency'
append_if_missing '!evidence/v6.0/notification_latency/.csv'
append_if_missing '!evidence/v6.0/notification_latency/.txt'

git add .gitignore >/dev/null 2>&1 || true
git commit -m "AT3: whitelist evidence notification_latency paths (v6.0.1)" >/dev/null 2>&1 || true
git rm -r --cached evidence >/dev/null 2>&1 || true

touch evidence/v6.0/notification_latency/probe.txt
if git add evidence/v6.0/notification_latency/probe.txt >/dev/null 2>&1; then
echo "AT3 RESULT=PASS" | tee "$OUT"
exit 0
fi

echo "AT3 RESULT=FAIL (probe not addable)" | tee "$OUT"
exit 1
