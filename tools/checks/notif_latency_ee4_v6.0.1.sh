#!/bin/sh
set -eu

OUT="evidence/v6.0/notification_latency/ee4.1.txt"
mkdir -p "$(dirname "$OUT")"

[ -f ".gitignore" ] || { echo "EE-4 RESULT=FAIL (.gitignore missing)" | tee "$OUT"; exit 2; }
git rev-parse --is-inside-work-tree >/dev/null 2>&1 || { echo "EE-4 RESULT=FAIL (not a git repo)" | tee "$OUT"; exit 3; }

ALLOW_GREP="$(grep -E '^( !evidence/|!evidence/|!evidence/v6\.0/|!evidence/v6\.0/notification_latency/|!evidence/.+\\)' .gitignore || true)"

check_not_ignored() {
p="$1"
if git check-ignore -n -- "$p" >/dev/null 2>&1; then
echo "ignored:$p"
fi
}

IGNORED="$(
check_not_ignored "evidence/"
check_not_ignored "evidence/v6.0/"
check_not_ignored "evidence/v6.0/notification_latency/"
check_not_ignored "evidence/v6.0/notification_latency/ee4.1.txt"
)"

{
echo "ALLOW_LINES_PRESENT: $( [ -n "$ALLOW_GREP" ] && echo YES || echo NO )"
echo "IGNORED_PATHS:"
if [ -n "$IGNORED" ]; then
printf '%s\n' "$IGNORED"
else
echo "[none]"
fi
} | tee "$OUT" >/dev/null

[ -n "$ALLOW_GREP" ] || { echo "EE-4 RESULT=FAIL (no whitelist lines for evidence/)" | tee -a "$OUT"; exit 1; }
[ -z "$IGNORED" ] || { echo "EE-4 RESULT=FAIL (some evidence paths are ignored)" | tee -a "$OUT"; exit 1; }

echo "EE-4 RESULT=PASS" | tee -a "$OUT"
exit 0
