#!/bin/bash
OUT="evidence/v6.0/_repo/git_pre_push_guard.2.txt"
exec > >(tee "$OUT") 2>&1

EXPECT_SHA="f87fa7f4f3213aa0fba6c2c19f94add5df77fde004840335b265202fd471084d"

echo "HOOKS_DIR:.git/hooks"
if [ -f ".git/hooks/pre-push" ]; then
  if [ -x ".git/hooks/pre-push" ]; then
    echo "PRE_PUSH:present+executable"
  else
    echo "PRE_PUSH:present+not-exec"
    echo "GIT-PRE-PUSH-GUARD RESULT=FAIL"
    exit 1
  fi
else
  echo "PRE_PUSH:absent"
  echo "GIT-PRE-PUSH-GUARD RESULT=FAIL"
  exit 1
fi

SHA="$(shasum -a 256 .git/hooks/pre-push | awk '{print $1}')"
echo "PRE_PUSH_SHA256:$SHA"

if [ "$SHA" != "$EXPECT_SHA" ]; then
  echo "HOOK_HASH_MISMATCH"
  echo "GIT-PRE-PUSH-GUARD RESULT=FAIL"
  exit 1
fi

echo "GIT-PRE-PUSH-GUARD RESULT=PASS"
exit 0
