#!/bin/bash
OUT="evidence/v6.0/_repo/git_pre_push_guard.1.txt"
exec > >(tee "$OUT") 2>&1

fail=0
echo "HOOKS_DIR:.git/hooks"
if [ -x ".git/hooks/pre-push" ]; then
  echo "PRE_PUSH:present+executable"
  sha="$(shasum -a 256 .git/hooks/pre-push 2>/dev/null | awk '{print $1}')"
  echo "PRE_PUSH_SHA256:$sha"
  fail=1
else
  echo "PRE_PUSH:absent_or_not_executable"
fi

hp="$(git config --get core.hooksPath || true)"
echo "CONFIG_hooksPath:${hp:-unset}"

if [ "$fail" -eq 0 ]; then
  echo "GIT-PRE-PUSH-GUARD RESULT=PASS"
  exit 0
else
  echo "GIT-PRE-PUSH-GUARD RESULT=FAIL"
  exit 1
fi
