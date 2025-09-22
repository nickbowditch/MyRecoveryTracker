#!/bin/bash
OUT="evidence/v6.0/_repo/git_pre_push_reveal.1.txt"
exec > >(tee "$OUT") 2>&1
HOOK=".git/hooks/pre-push"
LOCAL="$(git rev-parse HEAD 2>/dev/null || echo)"
REMOTE="$(git ls-remote origin -h refs/heads/main 2>/dev/null | awk '{print $1}')"
echo "HOOK:$HOOK"
[ -f "$HOOK" ] || { echo "HOOK_MISSING"; exit 1; }
echo "HOOK_X:$([ -x "$HOOK" ] && echo 1 || echo 0)"
echo "HOOK_SHA256:$(shasum -a 256 "$HOOK" | awk '{print $1}')"
echo "HOOK_BEGIN"; sed -n '1,200p' "$HOOK"; echo "HOOK_END"
printf "refs/heads/main %s refs/heads/main %s\n" "$LOCAL" "$REMOTE" | "$HOOK" origin git@github.com:nickbowditch/MyRecoveryTracker.git
RC=$?
echo "HOOK_RC:$RC"
exit $RC
