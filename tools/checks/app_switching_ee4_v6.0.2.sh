#!/bin/sh
set -eu

OUT="evidence/v6.0/app_switching/ee4.txt"
mkdir -p "$(dirname "$OUT")"

if ! git rev-parse --is-inside-work-tree >/dev/null 2>&1; then
{
echo "=== DEBUG ==="
echo "git_repo=NO"
echo ".gitignore_present=$( [ -f .gitignore ] && echo YES || echo NO )"
echo
echo "EE-4 RESULT=FAIL (not a git repo)"
} | tee "$OUT"
exit 1
fi

[ -f .gitignore ] || { echo "EE-4 RESULT=FAIL (.gitignore missing)" | tee "$OUT"; exit 1; }

ALLOW_GREP="$(grep -E '^( !evidence/|!evidence/|!evidence/v6\.0/|!evidence/v6\.0/app_switching/|!evidence/v6\.0/app_switching/\*\.txt)$' .gitignore || true)"

check_ignored() {
p="$1"
if git check-ignore -n -- "$p" >/dev/null 2>&1; then
echo "ignored:$p"
fi
}

IGNORED="$(
check_ignored "evidence/"
check_ignored "evidence/v6.0/"
check_ignored "evidence/v6.0/app_switching/"
check_ignored "evidence/v6.0/app_switching/ee4.probe.txt"
)"

{
echo "=== DEBUG ==="
echo "allow_lines_present=$( [ -n "$ALLOW_GREP" ] && echo YES || echo NO )"
echo ".gitignore_matches:"
if [ -n "$ALLOW_GREP" ]; then printf '%s\n' "$ALLOW_GREP"; else echo "[none]"; fi
echo
echo "ignored_paths:"
if [ -n "$IGNORED" ]; then printf '%s\n' "$IGNORED"; else echo "[none]"; fi
} | tee "$OUT" >/dev/null

[ -n "$ALLOW_GREP" ] || { echo "EE-4 RESULT=FAIL (no whitelist lines for evidence/)" | tee -a "$OUT"; exit 1; }
[ -z "$IGNORED" ] || { echo "EE-4 RESULT=FAIL (some evidence paths are ignored)" | tee -a "$OUT"; exit 1; }

echo "EE-4 RESULT=PASS" | tee -a "$OUT"
exit 0
