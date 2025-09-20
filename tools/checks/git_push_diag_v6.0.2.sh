#!/bin/bash
OUT="evidence/v6.0/_repo/git_push_diag.2.txt"
mkdir -p "$(dirname "$OUT")"
exec > >(tee "$OUT") 2>&1

echo "AUTH_SSH_BEGIN"
ssh -T git@github.com -o BatchMode=yes </dev/null || true
echo "AUTH_SSH_END"

echo "REMOTE_BEGIN"
git remote -v
git remote show origin || true
echo "REMOTE_END"

echo "FETCH_BEGIN"
git fetch -v origin && echo "FETCH_OK" || echo "FETCH_FAIL"
echo "FETCH_END"

echo "STATUS_BEGIN"
git status -sb || true
echo "STATUS_END"

echo "DIVERGENCE_BEGIN"
git rev-list --left-right --count origin/main...HEAD || true
echo "DIVERGENCE_END"

echo "PUSH_TAG_BEGIN"
git tag -f push-probe >/dev/null 2>&1 || true
git push -v origin refs/tags/push-probe && echo "PUSH_TAG_OK" || echo "PUSH_TAG_FAIL"
echo "PUSH_TAG_END"

echo "PUSH_MAIN_BEGIN"
git push -v origin HEAD:refs/heads/main && echo "PUSH_MAIN_OK" || echo "PUSH_MAIN_FAIL"
echo "PUSH_MAIN_END"

if grep -q 'PUSH_TAG_OK' "$OUT" && grep -q 'PUSH_MAIN_OK' "$OUT"; then
  echo "GIT-PUSH-DIAG RESULT=PASS"
  exit 0
else
  echo "GIT-PUSH-DIAG RESULT=FAIL"
  exit 1
fi
