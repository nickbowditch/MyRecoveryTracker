#!/bin/bash
OUT="evidence/v6.0/_repo/git_pre_push_inspect.1.txt"
exec > >(tee "$OUT") 2>&1

HOOK=".git/hooks/pre-push"
echo "HOOK_PATH:$HOOK"
[ -f "$HOOK" ] && echo "HOOK_PRESENT:1" || { echo "HOOK_PRESENT:0"; echo "GIT-PRE-PUSH-INSPECT RESULT=FAIL"; exit 1; }
[ -x "$HOOK" ] && echo "HOOK_EXEC:1" || echo "HOOK_EXEC:0"

SHA="$(shasum -a 256 "$HOOK" | awk '{print $1}')"
echo "HOOK_SHA256:$SHA"

LOCAL_SHA="$(git rev-parse HEAD 2>/dev/null || echo)"
REMOTE_SHA="$(git ls-remote origin -h refs/heads/main 2>/dev/null | awk '{print $1}')"
echo "LOCAL_SHA:$LOCAL_SHA"
echo "REMOTE_MAIN_SHA:$REMOTE_SHA"

printf "refs/heads/main %s refs/heads/main %s\n" "$LOCAL_SHA" "$REMOTE_SHA" | "$HOOK" origin git@github.com:nickbowditch/MyRecoveryTracker.git
RC=$?
echo "HOOK_EXIT:$RC"

if [ $RC -eq 0 ]; then
  echo "GIT-PRE-PUSH-INSPECT RESULT=PASS"
  exit 0
else
  echo "GIT-PRE-PUSH-INSPECT RESULT=FAIL"
  exit 1
fi
