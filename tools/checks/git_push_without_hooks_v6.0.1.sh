#!/bin/bash
OUT="evidence/v6.0/_repo/git_push_without_hooks.1.txt"
exec > >(tee "$OUT") 2>&1

ts=$(date +%s)
b="ci/probe-$ts"
t="probe-tag-$ts"

echo "REMOTE_BEGIN"
git remote -v
echo "REMOTE_END"

echo "FETCH_BEGIN"
git fetch -v origin && echo "FETCH_OK" || echo "FETCH_FAIL"
echo "FETCH_END"

echo "DIVERGENCE_BEGIN"
git rev-list --left-right --count origin/main...HEAD || true
echo "DIVERGENCE_END"

echo "PUSH_BRANCH_BEGIN"
git -c core.hooksPath=.git/hooks.empty push -v origin HEAD:refs/heads/$b && echo "PUSH_BRANCH_OK" || echo "PUSH_BRANCH_FAIL"
echo "PUSH_BRANCH_END"

echo "DEL_BRANCH_BEGIN"
git -c core.hooksPath=.git/hooks.empty push -v origin :refs/heads/$b >/dev/null 2>&1 || true
echo "DEL_BRANCH_END"

echo "PUSH_TAG_BEGIN"
git tag -f "$t" >/dev/null 2>&1 || true
git -c core.hooksPath=.git/hooks.empty push -v origin refs/tags/$t && echo "PUSH_TAG_OK" || echo "PUSH_TAG_FAIL"
echo "PUSH_TAG_END"

echo "DEL_TAG_BEGIN"
git -c core.hooksPath=.git/hooks.empty push -v origin :refs/tags/$t >/dev/null 2>&1 || true
git tag -d "$t" >/dev/null 2>&1 || true
echo "DEL_TAG_END"

okb=$(grep -c 'PUSH_BRANCH_OK' "$OUT" || true)
okt=$(grep -c 'PUSH_TAG_OK' "$OUT" || true)

if [ "$okb" -eq 1 ] && [ "$okt" -eq 1 ]; then
  echo "GIT-PUSH-WITHOUT-HOOKS RESULT=PASS"
  exit 0
else
  echo "GIT-PUSH-WITHOUT-HOOKS RESULT=FAIL"
  exit 1
fi
