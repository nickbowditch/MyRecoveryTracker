#!/bin/bash
OUT="evidence/v6.0/_repo/git_pre_push_trace.1.txt"
exec > >(tee "$OUT") 2>&1
set -x
HOOK=".git/hooks/pre-push"
LOCAL="$(git rev-parse HEAD 2>/dev/null || echo)"
REMOTE="$(git ls-remote origin -h refs/heads/main 2>/dev/null | awk '{print $1}')"
printf "refs/heads/main %s refs/heads/main %s\n" "$LOCAL" "$REMOTE" | bash -x "$HOOK" origin git@github.com:nickbowditch/MyRecoveryTracker.git
echo "RC:$?"
